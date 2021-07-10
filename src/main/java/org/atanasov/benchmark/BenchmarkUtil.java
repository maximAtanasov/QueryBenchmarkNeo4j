package org.atanasov.benchmark;

import org.neo4j.driver.summary.ProfiledPlan;

public class BenchmarkUtil {
    private BenchmarkUtil() {}

    public static long sumDbHits(ProfiledPlan profile) {
        if(profile == null) {
            return 0;
        }
        long dbHits = profile.dbHits();
        for(ProfiledPlan child : profile.children()){
            dbHits += sumDbHits(child);
        }
        return dbHits;
    }
}
