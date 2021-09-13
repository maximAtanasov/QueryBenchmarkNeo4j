package org.atanasov.benchmark.adjacency.query21;

import org.atanasov.benchmark.BenchmarkTemplate;
import org.atanasov.benchmark.ParameterConstants;
import org.atanasov.benchmark.Queries;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class KNeighborhood extends BenchmarkTemplate {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(KNeighborhood.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    public void query21() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_21,
                    Collections.singletonMap(ParameterConstants.PERSON_ID, 31999));
            return result.list();
        });
    }

    @Benchmark
    public void query21_2() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_21_2,
                    Collections.singletonMap(ParameterConstants.PERSON_ID, 31999));
            return result.list();
        });
    }

}
