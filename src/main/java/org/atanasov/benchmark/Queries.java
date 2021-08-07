package org.atanasov.benchmark;

public class Queries {
    private Queries() {}

    public static final String QUERY_1 = "MATCH (u:Person) WHERE u.id = $personId RETURN u LIMIT 1";
    public static final String QUERY_2 = "MATCH (u:Person) WHERE u.firstName = $firstName RETURN u";
    public static final String QUERY_3 = "MATCH (m:Message {id: $messageId}) " +
            "RETURN m.creationDate AS messageCreationDate, " +
            "coalesce(m.content, m.imageFile) AS messageContent";
    public static final String QUERY_4 = "MATCH (p)-[r:STUDY_AT]->() WHERE r.classYear > $classYear RETURN DISTINCT p";
    public static final String QUERY_9 = "MATCH (p)-[r:WORK_AT|STUDY_AT]->() RETURN DISTINCT p";
    public static final String QUERY_10 = "MATCH (p)-[r:WORK_AT]->() RETURN p " +
                                        "UNION " +
                                        "MATCH (p)-[:STUDY_AT]->() RETURN p";
    public static final String QUERY_11 = "MATCH ()<-[:WORK_AT]-(p:Person)-[:STUDY_AT]->() RETURN p";
    public static final String QUERY_12 = "MATCH ()<-[r1:WORK_AT]-(p:Person)-[r2:STUDY_AT]->() " +
            "USING SCAN r1:WORK_AT " +
            "USING SCAN r2:STUDY_AT " +
            "RETURN p";
    public static final String QUERY_13 = "MATCH ()<-[r1:WORK_AT]-(p:Person)-[:STUDY_AT]->() " +
            "USING SCAN r1:WORK_AT " +
            "RETURN p";
    public static final String QUERY_14 = "MATCH ()<-[:WORK_AT]-(p:Person)-[r1:STUDY_AT]->() " +
            "USING SCAN r1:STUDY_AT " +
            "RETURN p";
    public static final String QUERY_16 = "MATCH (p:Person {id: $personId}) WITH p LIMIT 1 " +
                                        "MATCH (m:Message) WHERE m.content CONTAINS p.firstName " +
                                        "RETURN p, collect(m) AS messages";
    public static final String QUERY_16_2 = "MATCH (p:Person {id: $personId}) WITH p LIMIT 1 " +
            "CALL db.index.fulltext.queryNodes('message_content', '*'+p.firstName+'*') " +
            "YIELD node AS m RETURN p, collect(m) AS messages";

    public static final String QUERY_19 = "MATCH (p1:Person {id: $personId1})-[:KNOWS]-(p2:Person {id:$personId2}) " +
            "RETURN COUNT(*) > 0";

    public static final String QUERY_19_2 = "MATCH (p:Person {id: $personId})-[:LIKES]->(m:Message {id:$messageId}) " +
            "RETURN COUNT(*) > 0";

    public static final String QUERY_19_3 = "MATCH (p:Person {id: $personId})-[:LIKES]->(m:Message {id:$messageId}) USING INDEX p:Person(id) " +
            "RETURN COUNT(*) > 0";

    public static final String QUERY_20 = "MATCH ()-[r1:LIKES {creationDate: $date1}]-(n) WITH r1 LIMIT 1 " +
            "MATCH ()-[r2:LIKES {creationDate: $date2}]-(n) WITH n LIMIT 1 RETURN COUNT (n) > 0";

    public static final String QUERY_20_2 = "MATCH ()-[r1:LIKES {creationDate: $date1}]-(n) USING SCAN r1:LIKES WITH r1 LIMIT 1 " +
            "MATCH ()-[r2:LIKES {creationDate: $date2}]-(n) USING SCAN r2:LIKES WITH n LIMIT 1 RETURN COUNT (n) > 0";

    public static final String QUERY_22 = "MATCH (n:Person {id: $personId})-[r:KNOWS]-(friend) " +
            "RETURN " +
            "friend.id AS personId, " +
            "friend.firstName AS firstName, " +
            "friend.lastName AS lastName, " +
            "r.creationDate AS friendshipCreationDate " +
            "ORDER BY friendshipCreationDate DESC, personId ASC";

    public static final String QUERY_28 = "MATCH (p1:Person {id: $personId})-[:KNOWS*4..7]->(p2:Person) RETURN DISTINCT p2";
}
