package org.atanasov.benchmark.adjacency.query20;

import org.apache.commons.math3.util.Pair;
import org.atanasov.benchmark.BenchmarkTemplate;
import org.atanasov.benchmark.ParameterConstants;
import org.atanasov.benchmark.Queries;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.logging.Level.INFO;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms8G", "-Xmx8G"})
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class BasicEdgeAdjacencyCheckIndex2 extends BenchmarkTemplate {

    private List<ZonedDateTime> dates;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BasicEdgeAdjacencyCheckIndex2.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() {
        var transaction = driver.session().beginTransaction();
        transaction.run("CREATE INDEX like_creation_date FOR ()-[r:LIKES]-() ON (r.creationDate)").consume();
        transaction.commit();
        transaction.close();

        transaction = driver.session().beginTransaction();
        dates = transaction.run("MATCH ()-[r:LIKES]-() RETURN r.creationDate as creationDate")
                .stream().map(value -> value.get("creationDate").asZonedDateTime()).collect(Collectors.toList());
        transaction.commit();
        transaction.close();

        //Use an array list to ensure access time with get() is constant
        dates = new ArrayList<>(dates);

        awaitIndexes();

        //Calculate DB Hits avg
        LOGGER.log(INFO, "QUERY 20.3 DBHITS: {0}",
                profileDbHits(Queries.QUERY_20_3, 5,
                        new Pair<>(ParameterConstants.DATE_1, dates),
                        new Pair<>(ParameterConstants.DATE_2, dates)));
    }

    @Benchmark
    public void query20_3Index() {
        Map<String, Object> params = new HashMap<>();
        params.put(ParameterConstants.DATE_1, dates.get(r.nextInt(dates.size())));
        params.put(ParameterConstants.DATE_2, dates.get(r.nextInt(dates.size())));

        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_20_3, params);
            return result.single();
        });
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        var transaction = driver.session().beginTransaction();
        transaction.run("DROP INDEX like_creation_date").consume();
        transaction.commit();
        transaction.close();
    }
}
