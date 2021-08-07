package org.atanasov.benchmark.patternmatch.basic.nodeproperty.query2;

import org.apache.commons.math3.util.Pair;
import org.atanasov.benchmark.BenchmarkTemplate;
import org.atanasov.benchmark.ParameterConstants;
import org.atanasov.benchmark.Queries;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.logging.Level.INFO;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class BasicPatternMatchIndexFirstName extends BenchmarkTemplate {

    private List<String> firstNames;

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

        transaction = driver.session().beginTransaction();
        firstNames = transaction.run("MATCH (p:Person) RETURN p.firstName as firstName")
                .stream().map(value -> value.get("firstName").asString()).collect(Collectors.toList());
        transaction.commit();
        transaction.close();

        awaitIndexes();

        //Calculate DB Hits avg
        LOGGER.log(INFO, "DBHITS: {0}",
                profileDbHits(Queries.QUERY_2, 100,
                        new Pair<>(ParameterConstants.FIRST_NAME, firstNames)));
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
                    Collections.singletonMap(ParameterConstants.FIRST_NAME, firstNames.get(r.nextInt(firstNames.size()))));
            return result.list();
        });
    }
}
