package org.atanasov.benchmark.reachability.varlength;

import org.atanasov.benchmark.BenchmarkTemplate;
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

import static java.util.logging.Level.INFO;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class VariableLengthPathBenchmarkNoIndex extends BenchmarkTemplate {

    private long[] personIds;

    private static final String QUERY_VAR_PATH = "MATCH (p:Person {id: $personId}) WITH p LIMIT 1 MATCH (p)-[:KNOWS*1..3]->(p2:Person) RETURN DISTINCT p2";
    private static final String QUERY_OPTIONAL_PATH = "MATCH (p:Person {id: $personId}) WITH p LIMIT 1 " +
            "MATCH (p)-[:KNOWS]->(p2:Person) WITH DISTINCT p2 " +
            "OPTIONAL MATCH (p2)-[:KNOWS]->(p3:Person) WITH DISTINCT collect(p2) as p2, p3 " +
            "OPTIONAL MATCH (p3)-[:KNOWS]->(p4:Person) WITH p2 + collect(p3) + collect(DISTINCT p4) as result " +
            "UNWIND result as p " +
            "RETURN DISTINCT p";
    private static final String QUERY_APOC = "MATCH (p:Person {id: $personId}) WITH p LIMIT 1 " +
            "CALL apoc.path.subgraphNodes(p, { " +
            "relationshipFilter: \"KNOWS>\"," +
            "    minLevel: 1, " +
            "    maxLevel: 3 " +
            "}) YIELD node RETURN node";


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(VariableLengthPathBenchmarkNoIndex.class.getSimpleName())
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
                    "PROFILE " + QUERY_VAR_PATH,
                    Collections.singletonMap(ParameterConstants.PERSON_ID, personId))
                    .consume().profile());
            transaction.commit();
            transaction.close();
        }
        LOGGER.log(INFO, "\nDBHITS: {0}", dbHits/100);

        //Optional query
        dbHits = 0;
        for(var i = 0; i < 100; i++) {
            transaction = driver.session().beginTransaction();
            long personId = personIds[r.nextInt(personIds.length)];
            dbHits += BenchmarkUtil.sumDbHits(transaction.run(
                    "PROFILE " + QUERY_OPTIONAL_PATH,
                    Collections.singletonMap(ParameterConstants.PERSON_ID, personId))
                    .consume().profile());
            transaction.commit();
            transaction.close();
        }
        LOGGER.log(INFO, "\nDBHITS: {0}", dbHits/100);

        //APOC query
        dbHits = 0;
        for(var i = 0; i < 100; i++) {
            transaction = driver.session().beginTransaction();
            long personId = personIds[r.nextInt(personIds.length)];
            dbHits += BenchmarkUtil.sumDbHits(transaction.run(
                    "PROFILE " + QUERY_APOC,
                    Collections.singletonMap(ParameterConstants.PERSON_ID, personId))
                    .consume().profile());
            transaction.commit();
            transaction.close();
        }
        LOGGER.log(INFO, "\nDBHITS: {0}", dbHits/100);
    }

    @Benchmark
    public void variableLengthQuery() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(QUERY_VAR_PATH,
                    Collections.singletonMap(ParameterConstants.PERSON_ID, personIds[r.nextInt(personIds.length)]));
            return result.list();
        });
    }

    @Benchmark
    public void variableLengthOptionalQuery() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(QUERY_OPTIONAL_PATH,
                    Collections.singletonMap(ParameterConstants.PERSON_ID, personIds[r.nextInt(personIds.length)]));
            return result.list();
        });
    }

    @Benchmark
    public void variableLengthAPOC() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(QUERY_APOC,
                    Collections.singletonMap(ParameterConstants.PERSON_ID, personIds[r.nextInt(personIds.length)]));
            return result.list();
        });
    }
}
