package org.atanasov.benchmark.patternmatch.id.message;

import org.atanasov.benchmark.BenchmarkUtil;
import org.atanasov.benchmark.ParameterConstants;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class BasicPatternMatchUniqueIndexMessageId {

    private final Driver driver= GraphDatabase.driver( "bolt://localhost", AuthTokens.basic( "neo4j", "neo3j" ) );

    private final Random r = new Random();
    private long[] messageIds;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BasicPatternMatchUniqueIndexMessageId.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() throws InterruptedException {
        var transaction = driver.session().beginTransaction();
        transaction.run("CREATE CONSTRAINT message_id ON (m:Message) ASSERT m.id IS UNIQUE").consume();
        transaction.commit();
        transaction.close();

        //Wait 60 secs for the index to populate
        Thread.sleep(60000);

        transaction = driver.session().beginTransaction();
        messageIds = transaction.run("MATCH (m:Message) RETURN m.id as messageId")
                .stream().mapToLong(value -> value.get("messageId").asLong()).toArray();
        transaction.commit();
        transaction.close();

        //Calculate DB Hits avg
        //Calculate DB Hits avg
        long dbHits = 0;
        for(var i = 0; i < 100; i++) {
            transaction = driver.session().beginTransaction();
            long messageId = messageIds[r.nextInt(messageIds.length)];
            dbHits += BenchmarkUtil.sumDbHits(transaction.run("PROFILE MATCH (u:Message) WHERE u.id = $messageId RETURN u LIMIT 1",
                    Collections.singletonMap(ParameterConstants.MESSAGE_ID, messageId))
                    .consume().profile());
            transaction.commit();
            transaction.close();
        }
        System.out.println("\nDBHITS: " + dbHits/100);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        var transaction = driver.session().beginTransaction();
        transaction.run("DROP CONSTRAINT message_id").consume();
        transaction.commit();
        transaction.close();
    }

    @Benchmark
    public void localQuery() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run("MATCH (u:Message) WHERE u.id = $messageId RETURN u LIMIT 1",
                    Collections.singletonMap(ParameterConstants.MESSAGE_ID, messageIds[r.nextInt(messageIds.length)]));
            return result.single();
        });
    }
}
