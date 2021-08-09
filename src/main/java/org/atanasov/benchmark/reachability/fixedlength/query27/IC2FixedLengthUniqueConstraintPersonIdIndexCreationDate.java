package org.atanasov.benchmark.reachability.fixedlength.query27;

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
public class IC2FixedLengthUniqueConstraintPersonIdIndexCreationDate extends BenchmarkTemplate {

    private List<Long> personIds;
    private List<ZonedDateTime> dates;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(IC2FixedLengthUniqueConstraintPersonIdIndexCreationDate.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() {
        var transaction = driver.session().beginTransaction();
        transaction.run("CREATE CONSTRAINT person_id ON (p:Person) ASSERT p.id IS UNIQUE").consume();
        transaction.commit();
        transaction.close();

        transaction = driver.session().beginTransaction();
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
                profileDbHits(Queries.QUERY_27_USING_INDEX, 10,
                        new Pair<>(ParameterConstants.PERSON_ID, personIds),
                        new Pair<>(ParameterConstants.MAX_DATE, dates)));
    }

    @Benchmark
    public void query27UniqueConstraintPersonIdIndexCreationDate() {
        Map<String, Object> params = new HashMap<>();
        params.put(ParameterConstants.PERSON_ID, personIds.get(r.nextInt(personIds.size())));
        params.put(ParameterConstants.MAX_DATE, dates.get(r.nextInt(dates.size())));

        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_27_USING_INDEX, params);
            return result.list();
        });
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        var transaction = driver.session().beginTransaction();
        transaction.run("DROP CONSTRAINT person_id").consume();
        transaction.commit();
        transaction.close();

        transaction = driver.session().beginTransaction();
        transaction.run("DROP INDEX message_creation_date").consume();
        transaction.commit();
        transaction.close();
    }
}
