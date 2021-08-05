package org.atanasov.benchmark;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import java.util.Random;
import java.util.logging.Logger;

public class BenchmarkTemplate {
    protected static final Logger LOGGER = Logger.getLogger(BenchmarkTemplate.class.getName());

    protected final Driver driver= GraphDatabase.driver( "bolt://localhost", AuthTokens.basic( "neo4j", "neo3j" ) );
    protected final Random r = new Random();

    protected void awaitIndexes() {
        var transaction = driver.session().beginTransaction();
        transaction.run("CALL db.awaitIndexes(1000)").consume();
        transaction.commit();
        transaction.close();
    }

}
