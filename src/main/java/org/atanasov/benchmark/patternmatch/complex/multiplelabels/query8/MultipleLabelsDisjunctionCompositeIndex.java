package org.atanasov.benchmark.patternmatch.complex.multiplelabels.query8;

import org.apache.commons.math3.util.Pair;
import org.atanasov.benchmark.BenchmarkTemplate;
import org.atanasov.benchmark.ParameterConstants;
import org.atanasov.benchmark.Queries;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.logging.Level.INFO;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class MultipleLabelsDisjunctionCompositeIndex extends BenchmarkTemplate {

    List<String> names;
    List<String> urls;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MultipleLabelsDisjunctionCompositeIndex.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() {
        var transaction = driver.session().beginTransaction();
        transaction.run("CREATE INDEX name_url FOR (p:City) ON (p.name, p.url)").consume();
        transaction.commit();
        transaction.close();

        transaction = driver.session().beginTransaction();
        names = transaction.run("MATCH (p:Place) RETURN p.name as name")
                .stream().map(value -> value.get("name").asString()).collect(Collectors.toList());
        transaction.commit();
        transaction.close();

        transaction = driver.session().beginTransaction();
        urls = transaction.run("MATCH (p:Place) RETURN p.url as url")
                .stream().map(value -> value.get("url").asString()).collect(Collectors.toList());
        transaction.commit();
        transaction.close();

        awaitIndexes();

        //Calculate DB Hits avg
        LOGGER.log(INFO, "DBHITS: {0}",
                profileDbHits(Queries.QUERY_8, 100,
                        new Pair<>(ParameterConstants.NAME, names),
                        new Pair<>(ParameterConstants.URL, urls)));
    }

    @Benchmark
    public void query6CompositeIndex() {
        Map<String, Object> params = new HashMap<>();
        params.put(ParameterConstants.NAME, names.get(r.nextInt(names.size())));
        params.put(ParameterConstants.URL, urls.get(r.nextInt(urls.size())));

        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_8, params);
            return result.list();
        });
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        var transaction = driver.session().beginTransaction();
        transaction.run("DROP INDEX name_url").consume();
        transaction.commit();
        transaction.close();
    }
}
