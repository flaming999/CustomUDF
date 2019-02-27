# CustomUDF
hive udf functions

## Compile
```
mvn compile
mvn package
```

## Usage
```
ADD JAR /home/hadoop/CustomUDF-1.0.jar;
CREATE TEMPORARY FUNCTION jsonpath AS 'com.warehouse.hive.udf.ParseJsonWithPath';
```
