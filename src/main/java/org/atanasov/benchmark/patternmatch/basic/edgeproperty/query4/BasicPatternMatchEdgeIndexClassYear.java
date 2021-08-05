package org.atanasov.benchmark.patternmatch.basic.edgeproperty.query4;

import org.atanasov.benchmark.BenchmarkTemplate;
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

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class BasicPatternMatchEdgeIndexClassYear extends BenchmarkTemplate {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BasicPatternMatchEdgeIndexClassYear.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() {
        var transaction = driver.session().beginTransaction();
        transaction.run("CREATE INDEX study_at_class_year FOR ()-[r:STUDY_AT]-() ON (r.classYear)").consume();
        transaction.commit();
        transaction.close();

        awaitIndexes();

        //Avg db hits: 348449 - calculated manually is Neo4jBrowser and incrementing classYear from 1990 to 2020 in intervals of 5
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        var transaction = driver.session().beginTransaction();
        transaction.run("DROP INDEX study_at_class_year").consume();
        transaction.commit();
        transaction.close();
    }

    @Benchmark
    public void query4Index() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_4,
                    Collections.singletonMap(ParameterConstants.CLASS_YEAR, 1990 + r.nextInt(30)));
            return result.list();
        });
    }
}
