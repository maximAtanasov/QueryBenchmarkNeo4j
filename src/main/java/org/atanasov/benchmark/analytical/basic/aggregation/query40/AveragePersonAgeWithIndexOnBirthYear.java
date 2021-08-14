package org.atanasov.benchmark.analytical.basic.aggregation.query40;

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
public class AveragePersonAgeWithIndexOnBirthYear extends BenchmarkTemplate {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(AveragePersonAgeWithIndexOnBirthYear.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() {
        //Calculate DB Hits avg
        LOGGER.log(INFO, "DBHITS: {0}",
                profileDbHits(Queries.QUERY_40_2, 5));
    }

    @Benchmark
    public void query40_2() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_40_2);
            return result.single();
        });
    }
}
