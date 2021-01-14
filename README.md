# kf-etl-indexer
Code copying data to elasticsearch using Spark

## Requirements
* Java 8 installed

## Branches
- branch *elasticsearch_6.x* supports ElasticSearch 6.8.13
- branch *elasticsearch_7.x* supports ElasticSearch 7.10.1

## Usage

### Choose a version

```shell
git checkout elasticsearch_6.x
git checkout elasticsearch_7.x
```

### Build the jar
```shell
sbt assembly
```

### Upload Jar to s3

```shell
aws s3 cp target/scala-2.11/kf-etl-indexer-$VERSION.jar s3://kf-strides-variant-parquet-prd/jobs/kf-etl-indexer-$VERSION.jar
```

### Run indexer
```shell
cd bin
./run_emr_job_indexer.sh release_id input es_nodes es_index_name es_index_template jarV number_instance instance_type
```
