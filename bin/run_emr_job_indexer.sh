#!/bin/bash
release_id=${1:-"re_000009"}
input=${2:-"s3a://kf-strides-variant-parquet-prd/tmp/variant_index_re_000009_SAMPLE"}
es_nodes=${3:-"https://vpc-kf-arranger-blue-es-service-exwupkrf4dyupg24dnfmvzcwri.us-east-1.es.amazonaws.com"}
es_index_name=${4:-"variant_index"}
es_index_template=${5:-"variant_index_template.json"}
jarV=${6:-"7.9.3"}
number_instance=${7:-"10"}
instance_type=${8:-"r5.4xlarge"}
env=${9:-"dev"}

# default is dev vpc-05be68d35774905e8
subnetId="subnet-0f822f9f9ff99871a"
serviceAccessSecurityGroup="sg-04894e9def6241eba"
emrManagedSlaveSecurityGroup="sg-0c131e9d64cec6a14"
emrManagedMasterSecurityGroup="sg-01a0dfc74131cff1d"
emrServiceRole=kf-variant-emr-prd-role # EMR_DefaultRole
ec2ProfileRole=kf-variant-emr-ec2-prd-profile #EMR_EC2_DefaultRole

#if env = prod
if [ ${env} == "prod" ]; then subnetId="subnet-00aab84919d5a44e2"; fi
if [ ${env} == "prod" ]; then serviceAccessSecurityGroup="sg-0587a1d20e24f4104"; fi
if [ ${env} == "prod" ]; then emrManagedSlaveSecurityGroup="sg-0dc6b48e674070821"; fi
if [ ${env} == "prod" ]; then emrManagedMasterSecurityGroup="sg-0a31895d33d1643da"; fi

steps=$(cat <<EOF
[
  {
    "Args": [
      "spark-submit",
      "--deploy-mode", "client",
      "--packages", "org.elasticsearch:elasticsearch-spark-20_2.11:${jarV},commons-httpclient:commons-httpclient:3.1",
      "--class", "org.kidsfirstdrc.variant.Indexer",
      "s3a://kf-strides-variant-parquet-prd/jobs/kf-etl-indexer-${jarV}.jar",
      "${input}",
      "${es_nodes}",
      "${es_index_name}",
      "${release_id}",
      "${es_index_template}"
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
ec2_attributes="{\"KeyName\":\"flintrock\",\"InstanceProfile\":\"${ec2ProfileRole}\",\"ServiceAccessSecurityGroup\":\"${serviceAccessSecurityGroup}\",\"SubnetId\":\"${subnetId}\",\"EmrManagedSlaveSecurityGroup\":\"${emrManagedSlaveSecurityGroup}\",\"EmrManagedMasterSecurityGroup\":\"${emrManagedMasterSecurityGroup}\"}"

aws emr create-cluster --applications Name=Hadoop Name=Spark \
--ec2-attributes "${ec2_attributes}" \
--service-role ${emrServiceRole} \
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

