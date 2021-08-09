package org.atanasov.benchmark.reachability.fixedlength.query25;

import org.apache.commons.math3.util.Pair;
import org.atanasov.benchmark.BenchmarkTemplate;
import org.atanasov.benchmark.ParameterConstants;
import org.atanasov.benchmark.Queries;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.logging.Level.INFO;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class FixedLengthUniqueConstraintPersonId extends BenchmarkTemplate {

    private List<Long> personIds;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FixedLengthUniqueConstraintPersonId.class.getSimpleName())
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
        LOGGER.log(INFO, "DBHITS Query 25: {0}",
                profileDbHits(Queries.QUERY_25, 1000,
                        new Pair<>(ParameterConstants.PERSON_ID, personIds)));

        //Calculate DB Hits avg
        LOGGER.log(INFO, "DBHITS Query 25.3: {0}",
                profileDbHits(Queries.QUERY_25_3, 1000,
                        new Pair<>(ParameterConstants.PERSON_ID, personIds)));
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        var transaction = driver.session().beginTransaction();
        transaction.run("DROP CONSTRAINT person_id").consume();
        transaction.commit();
        transaction.close();
    }

    @Benchmark
    public void query25UniqueConstraint() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_25,
                    Collections.singletonMap(ParameterConstants.PERSON_ID, personIds.get(r.nextInt(personIds.size()))));
            return result.list();
        });
    }

    @Benchmark
    public void query25_3UniqueConstraint() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_25_3,
                    Collections.singletonMap(ParameterConstants.PERSON_ID, personIds.get(r.nextInt(personIds.size()))));
            return result.list();
        });
    }
}
