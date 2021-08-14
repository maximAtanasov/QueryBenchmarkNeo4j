package org.atanasov.benchmark.analytical.basic.aggregation.query38;

import org.atanasov.benchmark.BenchmarkTemplate;
import org.atanasov.benchmark.Queries;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

import static java.util.logging.Level.INFO;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class MessageLengthIndex extends BenchmarkTemplate {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MessageLengthIndex.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() {
        var transaction = driver.session().beginTransaction();
        transaction.run("CREATE INDEX message_length FOR (m:Message) ON (m.length)").consume();
        transaction.commit();
        transaction.close();

        awaitIndexes();

        //Calculate DB Hits avg
        LOGGER.log(INFO, "DBHITS: {0}",
                profileDbHits(Queries.QUERY_38, 5));

        LOGGER.log(INFO, "DBHITS: {0}",
                profileDbHits(Queries.QUERY_38_2, 5));
    }

    @Benchmark
    public void query38Index() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_38);
            return result.single();
        });
    }

    @Benchmark
    public void query38_2Index() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_38_2);
            return result.single();
        });
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        var transaction = driver.session().beginTransaction();
        transaction.run("DROP INDEX message_length").consume();
        transaction.commit();
        transaction.close();
    }
}
