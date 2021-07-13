package org.atanasov.benchmark.patternmatch.id.person;

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
public class BasicPatternMatchNoIndexPersonId {

    private final Driver driver = GraphDatabase.driver( "bolt://localhost", AuthTokens.basic( "neo4j", "neo3j" ) );
    private long[] personIds;

    private final Random r = new Random();

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BasicPatternMatchNoIndexPersonId.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() {
        var transaction = driver.session().beginTransaction();
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
                    "PROFILE MATCH (u:Person) WHERE u.id = $personId RETURN u LIMIT 1",
                    Collections.singletonMap(ParameterConstants.PERSON_ID, personId))
                    .consume().profile());
            transaction.commit();
            transaction.close();
        }
        System.out.println("\nDBHITS: " + dbHits/100);
    }

    @Benchmark
    public void localQuery() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run("MATCH (u:Person) WHERE u.id = $personId RETURN u LIMIT 1",
                    Collections.singletonMap(ParameterConstants.PERSON_ID, personIds[r.nextInt(personIds.length)]));
            return result.single();
        });
    }
}
