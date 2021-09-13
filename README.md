# Neo4j JMH Query benchmark - LDBC-SF-10.0

Contains code for benchmarking various query types in Neo4j.
Must run against a local Neo4j instance (version 4.3.1) containing the LDBC-SNB dataset with a scale factor of 10.0.

# Running the benchmarks

 - Run `mvn install` in the project root
 - Run `java -Xmx4G -jar target/benchmarks.jar [BenchmarkClassName] -f 1 -wi 3 -i 10` where BenchMarkClassName is replaced with the class name of the required benchmark
 - If needed, modify the username/password values in the `BenchmarkTemplate` class to match those of your database instance.

Alternatively, an IDE such as IntelliJ may be used to run the benchmarks.
