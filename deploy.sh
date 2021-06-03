#!/bin/bash
tag=$1
folder=${2:-"qa"}
bucket=${3:-"s3://kf-strides-variant-parquet-prd"}

set -x
mkdir -p ~/.ivy2 ~/.sbt ~/.m2 ~/.sbt_cache
docker run --rm -v $(pwd):/app/build \
    --user $(id -u):$(id -g) \
    -v ~/.m2:/app/.m2 \
    -v ~/.ivy2:/app/.ivy2 \
    -v ~/.sbt:/app/.sbt \
    -v ~/.sbt_cache:/app/.cache \
    -w /app/build hseeberger/scala-sbt:8u282_1.5.2_2.11.12 \
    sbt -Duser.home=/app clean assembly

aws s3 cp target/scala-2.11/kf-etl-indexer-7.9.1.jar ${bucket}/jobs/${folder}/kf-etl-indexer-7.9.1.jar
aws s3 cp target/scala-2.11/kf-etl-indexer-7.9.1.jar ${bucket}/jobs/${folder}/kf-etl-indexer-7.9.1-${tag}.jar

aws s3 cp bin/templates ${bucket}/jobs/${folder}/templates --recursive