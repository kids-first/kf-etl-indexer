package org.kidsfirstdrc.variant

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col
import org.elasticsearch.spark.sql._

import scala.util.Try

object Indexer extends App {

  implicit val spark: SparkSession = SparkSession.builder
    .config("es.index.auto.create", "true")
    .config("es.nodes", args(1))
    .config("es.nodes.client.only", "false")
    .config("es.nodes.discovery", "false")
    .config("es.nodes.wan.only", "true")
    .config("es.read.ignore_exception",  "true")
    .config("es.port", "443")
    .config("es.wan.only", "true")
    .config("es.write.ignore_exception", "true")

    .config("spark.es.nodes.client.only", "false")
    .config("spark.es.nodes.wan.only", "true")
    .appName(s"Indexer").getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")

  println(s"ARGS: " + args.mkString("[", ", ", "]"))

  val Array(input, esNodes, indexName, release, templateFileName, jobType, columnId, chromosome) = args

  val ES_config =
    Map("es.mapping.id" -> columnId, "es.write.operation"-> jobType)

  val esClient = new ElasticSearchClient(esNodes.split(',').head)
  if (jobType == "index") {
    Try {
      println(s"ElasticSearch 'isRunning' status: [${esClient.isRunning}]")
      println(s"ElasticSearch 'checkNodes' status: [${esClient.checkNodeRoles}]")

      val respDelete = esClient.deleteIndex(s"${indexName}_$release")
      println(s"DELETE INDEX[${indexName}_$release] : " + respDelete.getStatusLine.getStatusCode + " : " + respDelete.getStatusLine.getReasonPhrase)
    }
    val response = esClient.setTemplate(s"s3://kf-strides-variant-parquet-prd/jobs/templates/$templateFileName")
    println(s"SET TEMPLATE[${templateFileName}] : " + response.getStatusLine.getStatusCode + " : " + response.getStatusLine.getReasonPhrase)

  }

  chromosome match {
    case "all" => spark.read.json(input).saveToEs(s"${indexName}_$release/_doc", ES_config)
    case s: String =>
      spark.read.json(input)
        .where(col("chromosome") === s)
        .saveToEs(s"${indexName}_${s}_$release/_doc", ES_config)
  }


}
