package org.atanasov.benchmark.reachability.varlength;

public class VariableLengthQueries {

    private VariableLengthQueries(){}

    public static final String QUERY_VAR_PATH = "MATCH (p:Person {id: $personId}) WITH p LIMIT 1 MATCH (p)-[:KNOWS*1..3]->(p2:Person) RETURN DISTINCT p2";
    public static final String QUERY_OPTIONAL_PATH = "MATCH (p:Person {id: $personId}) WITH p LIMIT 1 " +
            "MATCH (p)-[:KNOWS]->(p2:Person) WITH DISTINCT p2 " +
            "OPTIONAL MATCH (p2)-[:KNOWS]->(p3:Person) WITH DISTINCT collect(p2) as p2, p3 " +
            "OPTIONAL MATCH (p3)-[:KNOWS]->(p4:Person) WITH p2 + collect(p3) + collect(DISTINCT p4) as result " +
            "UNWIND result as p " +
            "RETURN DISTINCT p";

    public static final String QUERY_APOC = "MATCH (p:Person {id: $personId}) WITH p LIMIT 1 " +
            "CALL apoc.path.subgraphNodes(p, { " +
            "relationshipFilter: \"KNOWS>\"," +
            "    minLevel: 1, " +
            "    maxLevel: 3 " +
            "}) YIELD node RETURN node";

}
