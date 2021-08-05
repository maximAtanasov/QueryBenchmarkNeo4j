package org.atanasov.benchmark.patternmatch.complex.multiplepattern;

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
public class ComplexPatternMatchMultiplePatternsIndexPersonIdMessageContentLucene extends BenchmarkTemplate {

    private long[] personIds;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ComplexPatternMatchMultiplePatternsIndexPersonIdMessageContentLucene.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() {
        var transaction = driver.session().beginTransaction();
        transaction.run("CREATE CONSTRAINT person_id ON (p:Person) ASSERT p.id IS UNIQUE").consume();
        transaction.commit();
        transaction.close();

        transaction = driver.session().beginTransaction();
        transaction.run("CREATE INDEX message_content FOR (m:Message) ON (m.content) OPTIONS {indexProvider: 'lucene+native-3.0'}").consume();
        transaction.commit();
        transaction.close();

        awaitIndexes();

        transaction = driver.session().beginTransaction();
        personIds = transaction.run("MATCH (p:Person) RETURN p.id as personId")
                .stream().mapToLong(value -> value.get("personId").asLong()).toArray();
        transaction.commit();
        transaction.close();

        //Calculate DB Hits avg
        long dbHits = 0;
        for(var i = 0; i < 100; i++) {
            transaction = driver.session().beginTransaction();
            long personId = personIds[r.nextInt(personIds.length)];
            dbHits += BenchmarkUtil.sumDbHits(transaction.run(
                    "PROFILE " + Queries.QUERY_16,
                    Collections.singletonMap(ParameterConstants.PERSON_ID, personId))
                    .consume().profile());
            transaction.commit();
            transaction.close();
        }
        LOGGER.log(INFO, "\nDBHITS: {0}", dbHits/100);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        var transaction = driver.session().beginTransaction();
        transaction.run("DROP CONSTRAINT person_id").consume();
        transaction.commit();
        transaction.close();


        transaction = driver.session().beginTransaction();
        transaction.run("DROP INDEX message_content").consume();
        transaction.commit();
        transaction.close();
    }

    @Benchmark
    public void query13IndexPersonIdMessageContentLucene() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_16,
                    Collections.singletonMap(ParameterConstants.PERSON_ID, personIds[r.nextInt(personIds.length)]));
            return result.consume();
        });
    }
}
