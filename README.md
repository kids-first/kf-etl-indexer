# kf-etl-indexer
Code copying data to elasticsearch using Spark

## Requirements
* Java 8 installed

## Branches
branch *elasticsearch_6.x* supports ElasticSearch 6.8.13
branch *elasticsearch_7.x* supports ElasticSearch 7.10.1

## How to build the code

```shell
sbt assembly
```

## Upload Jar to s3

```shell
aws s3 cp target/scala-2.11/kf-etl-indexer-$VERSION.jar s3://kf-strides-variant-parquet-prd/jobs/kf-etl-indexer-$VERSION.jar
```

## How to run tests

```shell
sbt test
```
