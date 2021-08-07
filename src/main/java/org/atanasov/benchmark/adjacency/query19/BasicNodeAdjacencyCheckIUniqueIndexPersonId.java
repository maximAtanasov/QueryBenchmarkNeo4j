package org.atanasov.benchmark.adjacency.query19;

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
public class BasicNodeAdjacencyCheckIUniqueIndexPersonId extends BenchmarkTemplate {

    private List<Long> personIds;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BasicNodeAdjacencyCheckIUniqueIndexPersonId.class.getSimpleName())
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

        awaitIndexes();

        personIds = getPersonIds();

        //Calculate DB Hits avg
        LOGGER.log(INFO, "DBHITS: {0}",
                profileDbHits(Queries.QUERY_19_2, 100,
                        new Pair<>(ParameterConstants.PERSON_ID_1, personIds),
                        new Pair<>(ParameterConstants.PERSON_ID_2, personIds)));
    }

    @Benchmark
    public void query16UniqueConstraint() {
        Map<String, Object> params = new HashMap<>();
        params.put(ParameterConstants.PERSON_ID_1, personIds.get(r.nextInt(personIds.size())));
        params.put(ParameterConstants.PERSON_ID_2, personIds.get(r.nextInt(personIds.size())));

        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_19, params);
            return result.single();
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
