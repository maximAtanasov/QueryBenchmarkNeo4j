package org.atanasov.benchmark.reachability.fixedlength.query26;

import org.apache.commons.math3.util.Pair;
import org.atanasov.benchmark.BenchmarkTemplate;
import org.atanasov.benchmark.ParameterConstants;
import org.atanasov.benchmark.Queries;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.logging.Level.INFO;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class IC10FixedLengthUniqueConstraintPersonIdManualExpansion extends BenchmarkTemplate {

    private List<Long> personIds;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(IC10FixedLengthUniqueConstraintPersonIdManualExpansion.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() {
        var transaction = driver.session().beginTransaction();
        transaction.run("CREATE CONSTRAINT person_id ON (p:Person) ASSERT p.id IS UNIQUE").consume();
        transaction.commit();
        transaction.close();

        personIds = getPersonIds();

        awaitIndexes();

        //Calculate DB Hits avg
        LOGGER.log(INFO, "DBHITS: {0}",
                profileDbHits(Queries.QUERY_26_MANUAL_EXPANSION, 100,
                        new Pair<>(ParameterConstants.PERSON_ID, personIds),
                        new Pair<>(ParameterConstants.MONTH, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12))));
    }

    @Benchmark
    public void query26NoIndex() {
        Map<String, Object> params = new HashMap<>();
        params.put(ParameterConstants.PERSON_ID, personIds.get(r.nextInt(personIds.size())));
        params.put(ParameterConstants.MONTH, 1 + r.nextInt(12));

        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_26_MANUAL_EXPANSION, params);
            return result.list();
        });
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        var transaction = driver.session().beginTransaction();
        transaction.run("DROP CONSTRAINT person_id").consume();
        transaction.commit();
        transaction.close();
    }
}
