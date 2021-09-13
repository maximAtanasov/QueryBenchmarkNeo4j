package org.atanasov.benchmark.patternmatch.complex.multipleedge.query9_10;

import org.atanasov.benchmark.BenchmarkTemplate;
import org.atanasov.benchmark.Queries;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class MultipleEdgeMatchOr extends BenchmarkTemplate  {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MultipleEdgeMatchOr.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    public void query9() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_9);
            return result.list();
        });
    }

    @Benchmark
    public void query10() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_10);
            return result.list();
        });
    }
}
