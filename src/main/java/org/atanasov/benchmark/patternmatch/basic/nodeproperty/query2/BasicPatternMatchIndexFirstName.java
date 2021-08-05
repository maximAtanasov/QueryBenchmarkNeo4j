package org.atanasov.benchmark.patternmatch.basic.nodeproperty.query2;

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
public class BasicPatternMatchIndexFirstName extends BenchmarkTemplate {

    private String[] firstNames;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BasicPatternMatchIndexFirstName.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() {
        var transaction = driver.session().beginTransaction();
        transaction.run("CREATE INDEX person_first_name FOR (p:Person) ON (p.firstName)").consume();
        transaction.commit();
        transaction.close();

        awaitIndexes();

        transaction = driver.session().beginTransaction();
        firstNames = transaction.run("MATCH (p:Person) RETURN p.firstName as firstName")
                .stream().map(value -> value.get("firstName").asString()).toArray(String[]::new);
        transaction.commit();
        transaction.close();

        //Calculate DB Hits avg
        long dbHits = 0;
        for(var i = 0; i < 100; i++) {
            transaction = driver.session().beginTransaction();
            String firstName = firstNames[r.nextInt(firstNames.length)];
            dbHits += BenchmarkUtil.sumDbHits(transaction.run(
                    "PROFILE " + Queries.QUERY_2,
                    Collections.singletonMap(ParameterConstants.FIRST_NAME, firstName))
                    .consume().profile());
            transaction.commit();
            transaction.close();
        }
        LOGGER.log(INFO, "\nDBHITS: {0}", dbHits/100);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        var transaction = driver.session().beginTransaction();
        transaction.run("DROP INDEX person_first_name").consume();
        transaction.commit();
        transaction.close();
    }

    @Benchmark
    public void query2Index() {
        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_2,
                    Collections.singletonMap(ParameterConstants.FIRST_NAME, firstNames[r.nextInt(firstNames.length)]));
            return result.list();
        });
    }
}
