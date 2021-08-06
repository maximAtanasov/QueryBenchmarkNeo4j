package org.atanasov.benchmark.adjacency.query19_2;

import org.atanasov.benchmark.BenchmarkTemplate;
import org.atanasov.benchmark.BenchmarkUtil;
import org.atanasov.benchmark.ParameterConstants;
import org.atanasov.benchmark.Queries;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.logging.Level.INFO;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class BasicNodeAdjacencyCheckUniqueIndexPersonMessage extends BenchmarkTemplate {

    private long[] personIds;
    private long[] messageIds;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BasicNodeAdjacencyCheckUniqueIndexPersonMessage.class.getSimpleName())
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

        transaction = driver.session().beginTransaction();
        transaction.run("CREATE CONSTRAINT person_id ON (p:Person) ASSERT p.id IS UNIQUE").consume();
        transaction.commit();
        transaction.close();

        transaction = driver.session().beginTransaction();
        personIds = transaction.run("MATCH (p:Person) RETURN p.id as personId")
                .stream().mapToLong(value -> value.get("personId").asLong()).toArray();
        transaction.commit();
        transaction.close();

        transaction = driver.session().beginTransaction();
        messageIds = transaction.run("MATCH (m:Message) RETURN m.id as messageId")
                .stream().mapToLong(value -> value.get("messageId").asLong()).toArray();
        transaction.commit();
        transaction.close();

        awaitIndexes();

        //Calculate DB Hits avg
        long dbHits = 0;
        for(var i = 0; i < 100; i++) {
            transaction = driver.session().beginTransaction();
            long personId = personIds[r.nextInt(personIds.length)];
            long messageId = messageIds[r.nextInt(messageIds.length)];
            Map<String, Object> params = new HashMap<>();
            params.put(ParameterConstants.PERSON_ID, personId);
            params.put(ParameterConstants.MESSAGE_ID, messageId);

            dbHits += BenchmarkUtil.sumDbHits(transaction.run(
                    "PROFILE " + Queries.QUERY_19_3, params)
                    .consume().profile());
            transaction.commit();
            transaction.close();
        }
        LOGGER.log(INFO, "\nDBHITS: {0}", dbHits/100);
    }

    @Benchmark
    public void query19_2IndexPersonAndMessage() {
        Map<String, Object> params = new HashMap<>();
        params.put(ParameterConstants.PERSON_ID, personIds[r.nextInt(personIds.length)]);
        params.put(ParameterConstants.MESSAGE_ID, messageIds[r.nextInt(messageIds.length)]);

        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_19_3, params);
            return result.single();
        });
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        var transaction = driver.session().beginTransaction();
        transaction.run("DROP CONSTRAINT message_id").consume();
        transaction.commit();
        transaction.close();

        transaction = driver.session().beginTransaction();
        transaction.run("DROP CONSTRAINT person_id").consume();
        transaction.commit();
        transaction.close();
    }

}
