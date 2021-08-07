package org.atanasov.benchmark.patternmatch.basic.nodeproperty.query3;

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
public class BasicPatternMatchNoIndexMessageId extends BenchmarkTemplate {

    private List<Long> messageIds;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BasicPatternMatchNoIndexMessageId.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() {
        messageIds = getMessageIds();

        //Calculate DB Hits avg
        LOGGER.log(INFO, "DBHITS: {0}",
                profileDbHits(Queries.QUERY_3, 100,
                        new Pair<>(ParameterConstants.MESSAGE_ID, messageIds)));
    }

    @Benchmark
    public void query3() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_3,
                    Collections.singletonMap(ParameterConstants.MESSAGE_ID, messageIds.get(r.nextInt(messageIds.size()))));
            return result.single();
        });
    }
}
