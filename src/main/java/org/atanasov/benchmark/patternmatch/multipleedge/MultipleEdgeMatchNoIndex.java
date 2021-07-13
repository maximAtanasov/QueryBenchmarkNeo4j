package org.atanasov.benchmark.patternmatch.multipleedge;

import org.atanasov.benchmark.BenchmarkUtil;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
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
public class MultipleEdgeMatchNoIndex {

    private final Driver driver = GraphDatabase.driver( "bolt://localhost", AuthTokens.basic( "neo4j", "neo3j" ) );

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MultipleEdgeMatchNoIndex.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() {
        var transaction = driver.session().beginTransaction();

        //Calculate DB Hits avg
        transaction = driver.session().beginTransaction();
        long dbHits = BenchmarkUtil.sumDbHits(transaction.run(
                "PROFILE MATCH ()-[r:WORK_AT|STUDY_AT]->() RETURN r")
                .consume().profile());
        transaction.commit();
        transaction.close();
        System.out.println("\nDBHITS: " + dbHits);


        //Calculate DB Hits avg
        transaction = driver.session().beginTransaction();
        dbHits = BenchmarkUtil.sumDbHits(transaction.run(
                "PROFILE MATCH (p)-[r1:STUDY_AT]->() WITH collect(DISTINCT p) as resultStudy " +
                        "MATCH (p)-[r2:WORK_AT]->() WITH resultStudy + collect(DISTINCT p) as result " +
                        "UNWIND result AS p RETURN DISTINCT p")
                .consume().profile());
        transaction.commit();
        transaction.close();
        System.out.println("\nDBHITS: " + dbHits);
    }

    @Benchmark
    public void pathQuery1() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run("MATCH ()-[r:WORK_AT|STUDY_AT]->() RETURN r");
            return result.list();
        });
    }

    @Benchmark
    public void pathQuery2() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run("MATCH (p)-[r1:STUDY_AT]->() WITH collect(DISTINCT p) as resultStudy " +
                    "MATCH (p)-[r2:WORK_AT]->() WITH resultStudy + collect(DISTINCT p) as result " +
                    "UNWIND result AS p RETURN DISTINCT p");
            return result.list();
        });
    }
}
