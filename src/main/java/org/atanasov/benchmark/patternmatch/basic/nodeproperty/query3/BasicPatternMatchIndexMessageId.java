package org.atanasov.benchmark.patternmatch.basic.nodeproperty.query3;

import org.atanasov.benchmark.BenchmarkTemplate;
import org.atanasov.benchmark.BenchmarkUtil;
import org.atanasov.benchmark.ParameterConstants;
import org.atanasov.benchmark.Queries;
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

import static java.util.logging.Level.INFO;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class BasicPatternMatchIndexMessageId extends BenchmarkTemplate {

    private long[] messageIds;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BasicPatternMatchIndexMessageId.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() throws InterruptedException {
        var transaction = driver.session().beginTransaction();
        transaction.run("CREATE INDEX message_id FOR (m:Message) ON (m.id)").consume();
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
        long dbHits = 0;
        for(var i = 0; i < 100; i++) {
            transaction = driver.session().beginTransaction();
            long messageId = messageIds[r.nextInt(messageIds.length)];
            dbHits += BenchmarkUtil.sumDbHits(transaction.run(
                    "PROFILE " + Queries.QUERY_3,
                    Collections.singletonMap(ParameterConstants.MESSAGE_ID, messageId))
                    .consume().profile());
            transaction.commit();
            transaction.close();
        }
        LOGGER.log(INFO, "\nDBHITS: {0}", dbHits/100);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        var transaction = driver.session().beginTransaction();
        transaction.run("DROP INDEX message_id").consume();
        transaction.commit();
        transaction.close();
    }

    @Benchmark
    public void query3Index() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_3,
                    Collections.singletonMap(ParameterConstants.MESSAGE_ID, messageIds[r.nextInt(messageIds.length)]));
            return result.single();
        });
    }
}
