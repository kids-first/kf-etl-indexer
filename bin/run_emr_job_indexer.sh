#!/bin/bash
release_id=${1:-"re_000009"}
input=${2:-"s3a://kf-strides-variant-parquet-prd/tmp/variant_index_re_000009"}
es_nodes=${3:-"https://vpc-kf-arranger-blue-es-service-exwupkrf4dyupg24dnfmvzcwri.us-east-1.es.amazonaws.com:443"}
es_index_name=${4:-"variant_index"}
jarV=${5:-"7.9.3"}
number_instance=${6:-"2"}
instance_type=${7:-"r5.4xlarge"}

steps=$(cat <<EOF
[
  {
    "Args": [
      "spark-submit",
      "--deploy-mode", "client",
      "--packages", "org.elasticsearch:elasticsearch-spark-20_2.11:${jarV},commons-httpclient:commons-httpclient:3.1",
      "--class",
      "org.kidsfirstdrc.variant.Indexer",
      "s3a://kf-strides-variant-parquet-prd/jobs/kf-etl-indexer-${jarV}.jar",
      "${input}",
      "${es_nodes}",
      "${es_index_name}",
      "${release_id}"
    ],
    "Type": "CUSTOM_JAR",
    "ActionOnFailure": "TERMINATE_CLUSTER",
    "Jar": "command-runner.jar",
    "Properties": "",
    "Name": "Spark application"
  }
]
EOF
)

instance_groups="[{\"InstanceCount\":${number_instance},\"BidPrice\":\"OnDemandPrice\",\"EbsConfiguration\":{\"EbsBlockDeviceConfigs\":[{\"VolumeSpecification\":{\"SizeInGB\":150,\"VolumeType\":\"gp2\"},\"VolumesPerInstance\":8}],\"EbsOptimized\":true},\"InstanceGroupType\":\"CORE\",\"InstanceType\":\"${instance_type}\",\"Name\":\"Core - 2\"},{\"InstanceCount\":1,\"EbsConfiguration\":{\"EbsBlockDeviceConfigs\":[{\"VolumeSpecification\":{\"SizeInGB\":32,\"VolumeType\":\"gp2\"},\"VolumesPerInstance\":2}]},\"InstanceGroupType\":\"MASTER\",\"InstanceType\":\"m5.xlarge\",\"Name\":\"Master - 1\"}]"

aws emr create-cluster --applications Name=Hadoop Name=Spark \
--ec2-attributes '{"KeyName":"flintrock","InstanceProfile":"kf-variant-emr-ec2-prd-profile","ServiceAccessSecurityGroup":"sg-0587a1d20e24f4104","SubnetId":"subnet-00aab84919d5a44e2","EmrManagedSlaveSecurityGroup":"sg-0dc6b48e674070821","EmrManagedMasterSecurityGroup":"sg-0a31895d33d1643da"}' \
--service-role kf-variant-emr-prd-role \
--enable-debugging \
--release-label emr-5.32.0 \
--log-uri 's3n://kf-strides-variant-parquet-prd/jobs/elasticmapreduce/' \
--steps "${steps}" \
--name "Variant index - Release ${release_id}" \
--instance-groups "${instance_groups}" \
--scale-down-behavior TERMINATE_AT_TASK_COMPLETION \
--auto-terminate \
--configurations file://./spark-config.json \
--region us-east-1

