package org.atanasov.benchmark;

public class Queries {
    private Queries() {}

    public static final String QUERY_1 = "MATCH (u:Person) WHERE u.id = $personId RETURN u LIMIT 1";

    public static final String QUERY_2 = "MATCH (u:Person) WHERE u.firstName = $firstName RETURN u";

    public static final String QUERY_3 = "MATCH (m:Message {id: $messageId}) " +
            "RETURN m.creationDate AS messageCreationDate, " +
            "coalesce(m.content, m.imageFile) AS messageContent";

    public static final String QUERY_4 = "MATCH (p)-[r:STUDY_AT]->() WHERE r.classYear > $classYear RETURN DISTINCT p";

    public static final String QUERY_6 = "MATCH (c:City:Country) WHERE c.name = $name AND c.url = $url RETURN c";
    public static final String QUERY_8 =
            "MATCH (c) WHERE ((c:City) OR (c:Country)) AND c.name = $name AND c.url = $url RETURN c";

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

    public static final String QUERY_16 =
            "MATCH (p:Person {id: $personId}) WITH p LIMIT 1 " +
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

    public static final String QUERY_20 = "MATCH ()-[r1:LIKES {creationDate: $date1}]-(n)-[:LIKES {creationDate: $date2}]-() WITH n LIMIT 1 " +
            "RETURN COUNT (n) > 0";

    public static final String QUERY_20_2 = "MATCH ()-[r1:LIKES {creationDate: $date1}]-(n)-[r2:LIKES {creationDate: $date2}]-() " +
            "USING SCAN r1:LIKES WITH n LIMIT 1 " +
            "RETURN COUNT (n) > 0";

    public static final String QUERY_20_3 = "MATCH ()-[r1:LIKES {creationDate: $date1}]-(n)-[r2:LIKES {creationDate: $date2}]-() " +
            "USING INDEX r1:LIKES(creationDate) " +
            "USING INDEX r2:LIKES(creationDate) WITH n LIMIT 1 " +
            "RETURN COUNT (n) > 0";

    public static final String QUERY_21 = "MATCH (p1:Person {id: $personId})-[r:KNOWS*1..10]->(p2:Person) RETURN DISTINCT p2";

    public static final String QUERY_21_2 = "MATCH (p:Person {id: $personId}) " +
            "CALL apoc.path.subgraphNodes(p, { " +
            "relationshipFilter: \"KNOWS>\", minLevel: 1, maxLevel: 10 " +
            "}) " +
            "YIELD node RETURN node";

    public static final String QUERY_22 = "MATCH (n:Person {id: $personId})-[r:KNOWS]-(friend) " +
            "RETURN " +
            "friend.id AS personId, " +
            "friend.firstName AS firstName, " +
            "friend.lastName AS lastName, " +
            "r.creationDate AS friendshipCreationDate " +
            "ORDER BY friendshipCreationDate DESC, personId ASC";

    public static final String QUERY_25 = "MATCH (p1:Person {id: $personId})-[:KNOWS*4]->(p2:Person) RETURN DISTINCT p2";

    //k = 3
    public static final String QUERY_25_3 = "MATCH (p1:Person {id: $personId})-[:KNOWS]->()-[:KNOWS]->()" +
            "-[:KNOWS]->(p2:Person) " +
            "RETURN DISTINCT p2";

    public static final String QUERY_26 =
            "MATCH (person:Person {id: $personId})-[:KNOWS*2..2]-(friend:Person)-[:IS_LOCATED_IN]->(city:City) " +
            "WHERE " +
            "((friend.birthday.month = $month AND friend.birthday.day >= 21) OR " +
            "(friend.birthday.month = $month % 12 + 1 AND friend.birthday.day < 22)) " +
            "AND friend <> person " +
            "AND NOT (friend)-[:KNOWS]-(person) " +
            "WITH DISTINCT friend, city, person " +
            "OPTIONAL MATCH (friend)<-[:HAS_CREATOR]-(post:Post) " +
            "WITH friend, city, collect(post) AS posts, person " +
            "WITH " +
            "friend," +
            "city," +
            "size(posts) AS postCount," +
            "size([p IN posts WHERE (p)-[:HAS_TAG]->(:Tag)<-[:HAS_INTEREST]-(person)]) AS commonPostCount " +
            "RETURN " +
            "friend.id AS personId, " +
            "friend.firstName AS personFirstName, " +
            "friend.lastName AS personLastName, " +
            "commonPostCount - (postCount - commonPostCount) AS commonInterestScore, " +
            "friend.gender AS personGender, " +
            "city.name AS personCityName " +
            "ORDER BY commonInterestScore DESC, personId ASC " +
            "LIMIT 10";

    public static final String QUERY_26_MANUAL_EXPANSION =
            "MATCH (person:Person {id: $personId})-[:KNOWS]-(:Person)-[:KNOWS]-(friend:Person)-[:IS_LOCATED_IN]->(city:City) " +
            "WHERE " +
            "((friend.birthday.month = $month AND friend.birthday.day >= 21) OR " +
            "(friend.birthday.month = $month % 12 + 1 AND friend.birthday.day < 22)) " +
            "AND friend <> person " +
            "AND NOT (friend)-[:KNOWS]-(person) " +
            "WITH DISTINCT friend, city, person " +
            "OPTIONAL MATCH (friend)<-[:HAS_CREATOR]-(post:Post) " +
            "WITH friend, city, collect(post) AS posts, person " +
            "WITH " +
            "friend," +
            "city," +
            "size(posts) AS postCount," +
            "size([p IN posts WHERE (p)-[:HAS_TAG]->(:Tag)<-[:HAS_INTEREST]-(person)]) AS commonPostCount " +
            "RETURN " +
            "friend.id AS personId, " +
            "friend.firstName AS personFirstName, " +
            "friend.lastName AS personLastName, " +
            "commonPostCount - (postCount - commonPostCount) AS commonInterestScore, " +
            "friend.gender AS personGender, " +
            "city.name AS personCityName " +
            "ORDER BY commonInterestScore DESC, personId ASC " +
            "LIMIT 10";

    public static final String QUERY_27 =
            "MATCH (:Person {id: $personId})-[:KNOWS]-(friend:Person)<-[:HAS_CREATOR]-(message:Message) " +
            "WHERE message.creationDate < $maxDate " +
            "RETURN " +
            "friend.id AS personId, " +
            "friend.firstName AS personFirstName, " +
            "friend.lastName AS personLastName, " +
            "message.id AS messageId, " +
            "coalesce(message.content, message.imageFile) AS messageContent, " +
            "message.creationDate AS messageCreationDate " +
            "ORDER BY messageCreationDate DESC, messageId ASC " +
            "LIMIT 20";

    public static final String QUERY_27_USING_INDEX =
            "MATCH (:Person {id: $personId})-[:KNOWS]-(friend:Person)<-[:HAS_CREATOR]-(message:Message) USING INDEX message:Message(creationDate) " +
            "WHERE message.creationDate < $maxDate " +
            "RETURN " +
            "friend.id AS personId, " +
            "friend.firstName AS personFirstName, " +
            "friend.lastName AS personLastName, " +
            "message.id AS messageId, " +
            "coalesce(message.content, message.imageFile) AS messageContent, " +
            "message.creationDate AS messageCreationDate " +
            "ORDER BY messageCreationDate DESC, messageId ASC " +
            "LIMIT 20";

    public static final String QUERY_29 = "MATCH " +
            "(:Person {id: $personId})-[:KNOWS*1..2]-(otherPerson:Person)<-[:HAS_CREATOR]-(message:Message) " +
            "WHERE message.creationDate < $maxDate " +
            "RETURN DISTINCT " +
            "otherPerson.id AS otherPersonId, " +
            "otherPerson.firstName AS otherPersonFirstName, " +
            "otherPerson.lastName AS otherPersonLastName, " +
            "message.id AS messageId, " +
            "coalesce(message.content, message.imageFile) AS messageContent, " +
            "message.creationDate AS messageCreationDate " +
            "ORDER BY message.creationDate DESC, message.id ASC " +
            "LIMIT 20";

    public static final String QUERY_29_USING_INDEX = "MATCH " +
            "(p:Person {id: $personId})-[:KNOWS*1..2]-(otherPerson:Person)<-[:HAS_CREATOR]-(message:Message) " +
            "USING INDEX p:Person(id) " +
            "USING INDEX message:Message(creationDate) " +
            "WHERE message.creationDate < $maxDate " +
            "RETURN DISTINCT " +
            "otherPerson.id AS otherPersonId, " +
            "otherPerson.firstName AS otherPersonFirstName, " +
            "otherPerson.lastName AS otherPersonLastName, " +
            "message.id AS messageId, " +
            "coalesce(message.content, message.imageFile) AS messageContent, " +
            "message.creationDate AS messageCreationDate " +
            "ORDER BY message.creationDate DESC, message.id ASC " +
            "LIMIT 20";


    public static final String QUERY_34 = "MATCH (person1:Person {id: $personId1}), (person2:Person {id: $personId2}) " +
            "OPTIONAL MATCH path = shortestPath((person1)-[:KNOWS*]-(person2)) " +
            "RETURN " +
            "CASE path IS NULL " +
            "  WHEN true THEN -1 " +
            "  ELSE length(path) " +
            "END AS shortestPathLength";

    public static final String QUERY_38 = "MATCH (m:Message) RETURN min(m.length)";

    public static final String QUERY_38_2 = "MATCH (m:Message) RETURN max(m.length)";

    public static final String QUERY_39 = "MATCH (m:Message) RETURN avg(m.length)";

    public static final String QUERY_40 =
            "MATCH (n:Person) WITH avg(n.birthday.year) as avgYear " +
            "RETURN date.truncate('year').year - avgYear";

    public static final String QUERY_40_2 =
            "MATCH (n:Person) WITH avg(n.birthYear) as avgYear " +
                    "RETURN date.truncate('year').year - avgYear";

    public static final String QUERY_48 = "MATCH (m:Message) RETURN sum(m.length)";

}
