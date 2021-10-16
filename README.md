# Neo4j JMH Query benchmark - LDBC-SF-10.0

Contains code for benchmarking various query types in Neo4j.
Must run against a local Neo4j instance (version 4.3.1) containing the LDBC-SNB dataset with a scale factor of 10.0.

## Importing the LDBC-SNB dataset into Neo4j

The steps to doing so are as follows:

NOTE: If you are running the following steps on Windows, you must use Git Bash or preferably WSL2 in order to run the scripts.

 - The first step is to clone the [LDBC SNB Interactive](https://github.com/ldbc/ldbc_snb_interactive) and [LDBC SNB Datagen Hadoop](https://github.com/ldbc/ldbc_snb_datagen_hadoop_hadoop) repositories. 
 - Generate the data in CSV format using the LDBC-SNB Hadoop data generator. This can be done by following the instructions in the [SNB Interactive](https://github.com/ldbc/ldbc_snb_interactive/tree/main/cypher#generating-the-data-set) repository.
 - After generating the required CSV files you must move them to the `cypher/test-data/vanilla` directory of the SNB Interactive repository.
 - After doing so, run the `environment-variables-default.sh` shell script located in the `scripts` directory. This will set the environment variables required by the next script.
 - Run `the convert-csvs.sh` script from the `scripts` directory.
 - The converted CSV files should be located in the `test-data/converted` directory.
 - To import these CSV files into Neo4j, you can use the `neo4j-admin` tool located in the `bin` directory of your Neo4j installation. To do so, move the CSV files to the root directory of the database and run the following command: 
```
bin/neo4j-admin.[sh/bat/ps1] import --database=neo4j -id-type=INTEGER 
--nodes=Message:Comment="comment_0_0.csv" --nodes=Forum="forum_0_0.csv" 
--nodes=Organisation="organisation_0_0.csv"  
--nodes=Person="person_0_0.csv" --nodes=Place="place_0_0.csv" 
--nodes=Message:Post="post_0_0.csv" --nodes=TagClass="tagclass_0_0.csv" 
--nodes=Tag="tag_0_0.csv" 
--relationships=HAS_CREATOR="comment_hasCreator_person_0_0.csv" 
--relationships=IS_LOCATED_IN="comment_isLocatedIn_place_0_0.csv" 
--relationships=REPLY_OF="comment_replyOf_comment_0_0.csv"  
--relationships=REPLY_OF="comment_replyOf_post_0_0.csv"  
--relationships=CONTAINER_OF="forum_containerOf_post_0_0.csv"   
--relationships=HAS_MEMBER="forum_hasMember_person_0_0.csv" 
--relationships=HAS_MODERATOR="forum_hasModerator_person_0_0.csv" 
--relationships=HAS_TAG="forum_hasTag_tag_0_0.csv" 
--relationships=HAS_INTEREST="person_hasInterest_tag_0_0.csv" 
--relationships=IS_LOCATED_IN="person_isLocatedIn_place_0_0.csv" 
--relationships=KNOWS="person_knows_person_0_0.csv" 
--relationships=LIKES="person_likes_comment_0_0.csv" 
--relationships=LIKES="person_likes_post_0_0.csv" 
--relationships=IS_PART_OF="place_isPartOf_place_0_0.csv" 
--relationships=HAS_CREATOR="post_hasCreator_person_0_0.csv" 
--relationships=HAS_TAG="comment_hasTag_tag_0_0.csv" 
--relationships=HAS_TAG="post_hasTag_tag_0_0.csv" 
--relationships=IS_LOCATED_IN="post_isLocatedIn_place_0_0.csv" 
--relationships=IS_SUBCLASS_OF="tagclass_isSubclassOf_tagclass_0_0.csv" 
--relationships=HAS_TYPE="tag_hasType_tagclass_0_0.csv" 
--relationships=STUDY_AT="person_studyAt_organisation_0_0.csv" 
--relationships=WORK_AT="person_workAt_organisation_0_0.csv" 
--relationships=IS_LOCATED_IN="organisation_isLocatedIn_place_0_0.csv" 
--delimiter='U+007C'
```
- If you are running Neo4j in a docker container, use the instructions in the [SNB Repository](https://github.com/ldbc/ldbc_snb_interactive/tree/main/cypher#load-the-data-set)

## Running the benchmarks

 - Run `mvn install` in the project root
 - Run `java -Xmx4G -jar target/benchmarks.jar [BenchmarkClassName] -f 1 -wi 3 -i 10` where BenchMarkClassName is replaced with the class name of the required benchmark
 - If needed, modify the username/password values in the `BenchmarkTemplate` class to match those of your database instance.

Alternatively, an IDE such as IntelliJ may be used to run the benchmarks.

