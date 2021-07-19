# kf-etl-indexer
Code copying data to elasticsearch using Spark

## Requirements
* Java 8 installed

## Branches
- branch master supports ElasticSearch 7.9.1+

## Usage

### Build the jar
```shell
sbt assembly
```

### Upload Jar to s3

```shell
# for spark 2
aws s3 cp target/scala-2.11/kf-etl-indexer-$VERSION.jar s3://kf-strides-variant-parquet-prd/jobs/kf-etl-indexer-$VERSION.jar
# for spark 3
aws s3 cp target/scala-2.12/kf-etl-indexer-spark3-$VERSION.jar s3://kf-strides-variant-parquet-prd/jobs/kf-etl-indexer-spark3-$VERSION.jar
```

### Run indexer
```shell
cd bin
# for spark 2
./run_emr_job_indexer.sh
# for spark 3
./run_emr_job_indexer_spark3.sh
```
