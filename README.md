# kf-etl-indexer
Code copying data to elasticsearch using Spark

## Requirements
* Java 8 installed
* Elasticsearch 7.x up and running (tested only with 7.10)

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
