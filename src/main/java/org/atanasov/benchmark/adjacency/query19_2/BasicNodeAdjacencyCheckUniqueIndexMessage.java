package org.atanasov.benchmark.adjacency.query19_2;

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
public class BasicNodeAdjacencyCheckUniqueIndexMessage extends BenchmarkTemplate {

    private List<Long> personIds;
    private List<Long> messageIds;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BasicNodeAdjacencyCheckUniqueIndexMessage.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() {
        var transaction = driver.session().beginTransaction();
        transaction.run("CREATE CONSTRAINT message_id ON (m:Message) ASSERT m.id IS UNIQUE").consume();
        transaction.commit();
        transaction.close();

        personIds = getPersonIds();
        messageIds = getMessageIds();

        awaitIndexes();

        //Calculate DB Hits avg
        LOGGER.log(INFO, "DBHITS: {0}",
                profileDbHits(Queries.QUERY_19_2, 100,
                        new Pair<>(ParameterConstants.PERSON_ID, personIds),
                        new Pair<>(ParameterConstants.MESSAGE_ID, messageIds)));
    }

    @Benchmark
    public void query19_2IndexMessage() {
        Map<String, Object> params = new HashMap<>();
        params.put(ParameterConstants.PERSON_ID, personIds.get(r.nextInt(personIds.size())));
        params.put(ParameterConstants.MESSAGE_ID, messageIds.get(r.nextInt(messageIds.size())));

        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_19_2, params);
            return result.single();
        });
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        var transaction = driver.session().beginTransaction();
        transaction.run("DROP CONSTRAINT message_id").consume();
        transaction.commit();
        transaction.close();
    }

}
