package org.atanasov.benchmark.reachability.varlength;

import org.apache.commons.math3.util.Pair;
import org.atanasov.benchmark.BenchmarkTemplate;
import org.atanasov.benchmark.ParameterConstants;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.logging.Level.INFO;
import static org.atanasov.benchmark.Queries.*;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class VariableLengthPathBenchmarkUniqueIndex extends BenchmarkTemplate {

    private List<Long> personIds;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(VariableLengthPathBenchmarkUniqueIndex.class.getSimpleName())
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
                profileDbHits(QUERY_28, 100,
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
    public void variableLengthQuery() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(QUERY_28,
                    Collections.singletonMap(ParameterConstants.PERSON_ID, personIds.get(r.nextInt(personIds.size()))));
            return result.list();
        });
    }
}
