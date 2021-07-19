package org.atanasov.benchmark;

public class Queries {
    private Queries() {}

    public static final String QUERY_1 = "MATCH (u:Person) WHERE u.id = $personId RETURN u LIMIT 1";
    public static final String QUERY_2 = "MATCH (u:Person) WHERE u.firstName = $firstName RETURN u";
    public static final String QUERY_3 = "MATCH (m:Message {id: $messageId}) " +
            "RETURN m.creationDate AS messageCreationDate, " +
            "coalesce(m.content, m.imageFile) AS messageContent";
    public static final String QUERY_4 = "MATCH (p)-[r:STUDY_AT]->() WHERE r.classYear > $classYear RETURN DISTINCT p";
    public static final String QUERY_6 = "MATCH (p)-[r:WORK_AT|STUDY_AT]->() RETURN DISTINCT p";
    public static final String QUERY_7 = "MATCH (p)-[r:WORK_AT]->() RETURN p " +
                                        "UNION " +
                                        "MATCH (p)-[:STUDY_AT]->() RETURN p";
    public static final String QUERY_8 = "MATCH ()<-[:WORK_AT]-(p:Person)-[:STUDY_AT]->() RETURN p";
    public static final String QUERY_9 = "MATCH ()<-[r1:WORK_AT]-(p:Person)-[r2:STUDY_AT]->() " +
            "USING SCAN r1:WORK_AT " +
            "USING SCAN r2:STUDY_AT " +
            "RETURN p";
    public static final String QUERY_10 = "MATCH ()<-[r1:WORK_AT]-(p:Person)-[:STUDY_AT]->() " +
            "USING SCAN r1:WORK_AT " +
            "RETURN p";
    public static final String QUERY_11 = "MATCH ()<-[:WORK_AT]-(p:Person)-[r1:STUDY_AT]->() " +
            "USING SCAN r1:STUDY_AT " +
            "RETURN p";
    public static final String QUERY_13 = "MATCH (p:Person {id: $personId}) WITH p LIMIT 1 " +
                                        "MATCH (m:Message) WHERE m.content CONTAINS p.firstName " +
                                        "RETURN p, collect(m) AS messages";
    public static final String QUERY_13_2 = "MATCH (p:Person {id: $personId}) WITH p LIMIT 1 " +
            "CALL db.index.fulltext.queryNodes('message_content', '*'+p.firstName+'*') " +
            "YIELD node AS m RETURN p, collect(m) AS messages";

}
