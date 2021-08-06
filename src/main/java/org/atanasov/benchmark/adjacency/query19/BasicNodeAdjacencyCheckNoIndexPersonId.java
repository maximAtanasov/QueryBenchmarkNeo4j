package org.atanasov.benchmark.adjacency.query19;

import org.atanasov.benchmark.BenchmarkTemplate;
import org.atanasov.benchmark.BenchmarkUtil;
import org.atanasov.benchmark.ParameterConstants;
import org.atanasov.benchmark.Queries;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.logging.Level.INFO;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class BasicNodeAdjacencyCheckNoIndexPersonId extends BenchmarkTemplate {

    private long[] personIds;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BasicNodeAdjacencyCheckNoIndexPersonId.class.getSimpleName())
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
            long personId1 = personIds[r.nextInt(personIds.length)];
            long personId2 = personIds[r.nextInt(personIds.length)];
            Map<String, Object> params = new HashMap<>();
            params.put(ParameterConstants.PERSON_ID_1, personId1);
            params.put(ParameterConstants.PERSON_ID_2, personId2);

            dbHits += BenchmarkUtil.sumDbHits(transaction.run(
                    "PROFILE " + Queries.QUERY_19, params)
                    .consume().profile());
            transaction.commit();
            transaction.close();
        }
        LOGGER.log(INFO, "\nDBHITS: {0}", dbHits/100);
    }

    @Benchmark
    public void query19() {
        Map<String, Object> params = new HashMap<>();
        params.put(ParameterConstants.PERSON_ID_1, personIds[r.nextInt(personIds.length)]);
        params.put(ParameterConstants.PERSON_ID_2, personIds[r.nextInt(personIds.length)]);

        driver.session().readTransaction(transaction -> {
            var result = transaction.run(Queries.QUERY_19, params);
            return result.single();
        });
    }
}