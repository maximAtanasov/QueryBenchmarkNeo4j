package org.atanasov.benchmark.patternmatch.basic.edgeproperty.query4;

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
public class BasicPatternMatchEdgeNoIndexClassYear {

    private final Driver driver = GraphDatabase.driver( "bolt://localhost", AuthTokens.basic( "neo4j", "neo3j" ) );

    private final Random r = new Random();

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BasicPatternMatchEdgeNoIndexClassYear.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() {
        //Avg db hits: 1106843 - calculated manually is Neo4jBrowser and incrementing classYear from 1990 to 2020 in intervals of 5
    }

    @Benchmark
    public void query4() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_4,
                    Collections.singletonMap(ParameterConstants.CLASS_YEAR, 1990 + r.nextInt(30)));
            return result.list();
        });
    }
}
