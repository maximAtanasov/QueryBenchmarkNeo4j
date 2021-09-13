package org.atanasov.benchmark;

import org.apache.commons.math3.util.Pair;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.summary.ProfiledPlan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BenchmarkTemplate {
    protected static final Logger LOGGER = Logger.getLogger(BenchmarkTemplate.class.getName());

    protected final Driver driver= GraphDatabase.driver( "bolt://localhost", AuthTokens.basic( "neo4j", "neo3j" ) );
    protected final Random r = new Random(1L);

    protected void awaitIndexes() {
        var transaction = driver.session().beginTransaction();
        transaction.run("CALL db.awaitIndexes(1000)").consume();
        transaction.commit();
        transaction.close();
    }

    private long sumDbHits(ProfiledPlan profile) {
        if(profile == null) {
            return 0;
        }
        long dbHits = profile.dbHits();
        for(ProfiledPlan child : profile.children()){
            dbHits += sumDbHits(child);
        }
        return dbHits;
    }


    @SafeVarargs
    protected final long profileDbHits(String query, int iterations, Pair<String, List<?>>... params) {
        long dbHits = 0;
        Transaction transaction;
        for(var i = 0; i < iterations; i++) {
            Map<String, Object> paramsMap = new HashMap<>();
            for(Pair<String, List<?>> param : params) {
                paramsMap.put(param.getFirst(), param.getSecond().get(r.nextInt(param.getSecond().size())));
            }
            transaction = driver.session().beginTransaction();
            dbHits += sumDbHits(transaction.run(
                    "PROFILE " + query, paramsMap)
                    .consume().profile());
            transaction.commit();
            transaction.close();
        }
        return dbHits/iterations;
    }

    protected List<Long> getMessageIds() {
        var transaction = driver.session().beginTransaction();
        List<Long> messageIds = transaction.run("MATCH (m:Message) RETURN m.id as messageId")
                .stream().mapToLong(value -> value.get("messageId").asLong()).boxed().collect(Collectors.toList());
        transaction.commit();
        transaction.close();
        return messageIds;
    }

    protected List<Long> getPersonIds() {
        var transaction = driver.session().beginTransaction();
        List<Long> personIds = transaction.run("MATCH (p:Person) RETURN p.id as personId")
                .stream().mapToLong(value -> value.get("personId").asLong()).boxed().collect(Collectors.toList());
        transaction.commit();
        transaction.close();
        return personIds;
    }
}
