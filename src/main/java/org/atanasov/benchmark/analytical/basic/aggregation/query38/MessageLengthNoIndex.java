package org.atanasov.benchmark.analytical.basic.aggregation.query38;

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
public class MessageLengthNoIndex extends BenchmarkTemplate {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MessageLengthNoIndex.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() {
        //Calculate DB Hits avg
        LOGGER.log(INFO, "DBHITS: {0}",
                profileDbHits(Queries.QUERY_38, 5));

        LOGGER.log(INFO, "DBHITS: {0}",
                profileDbHits(Queries.QUERY_38_2, 5));
    }

    @Benchmark
    public void query38() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_38);
            return result.single();
        });
    }

    @Benchmark
    public void query38_2() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_38_2);
            return result.single();
        });
    }
}
