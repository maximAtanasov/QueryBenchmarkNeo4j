package org.atanasov.benchmark.reachability.varlength.query29;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.logging.Level.INFO;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms3G", "-Xmx3G"})
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class IC9FixedLengthIndexMessageCreationDate extends BenchmarkTemplate {

    private List<Long> personIds;
    private List<ZonedDateTime> dates;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(IC9FixedLengthIndexMessageCreationDate.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() {
        var transaction = driver.session().beginTransaction();
        transaction.run("CREATE INDEX message_creation_date FOR (m:Message) ON (m.creationDate)").consume();
        transaction.commit();
        transaction.close();

        personIds = getPersonIds();

        transaction = driver.session().beginTransaction();
        dates = transaction.run("MATCH (m:Message) RETURN m.creationDate as creationDate LIMIT 2000")
                .stream().map(value -> value.get("creationDate").asZonedDateTime()).collect(Collectors.toList());
        transaction.commit();
        transaction.close();

        awaitIndexes();

        //Calculate DB Hits avg
        LOGGER.log(INFO, "DBHITS: {0}",
                profileDbHits(Queries.QUERY_29, 5,
                        new Pair<>(ParameterConstants.PERSON_ID, personIds),
                        new Pair<>(ParameterConstants.MAX_DATE, dates)));
    }

    @Benchmark
    public void query29IndexMessageCreationDate() {
        Map<String, Object> params = new HashMap<>();
        params.put(ParameterConstants.PERSON_ID, personIds.get(r.nextInt(personIds.size())));
        params.put(ParameterConstants.MAX_DATE, dates.get(r.nextInt(dates.size())));

        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_29, params);
            return result.list();
        });
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        var transaction = driver.session().beginTransaction();
        transaction.run("DROP INDEX message_creation_date").consume();
        transaction.commit();
        transaction.close();
    }
}
