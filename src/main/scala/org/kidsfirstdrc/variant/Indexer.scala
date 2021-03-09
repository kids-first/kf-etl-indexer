package org.kidsfirstdrc.variant

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}
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

  val Array(input, esNodes, indexName, release, templateFileName, jobType) = args

  val ES_config =
    Map("es.mapping.id" -> "hash", "es.write.operation"-> jobType)

  def run(df: DataFrame, indexName: String)(implicit spark: SparkSession): Unit = {
    import spark.implicits._

    //creates the columns `hash` if the column doesn't already exists.
    val dfWithId =
      df.columns.find(_.equals("hash")).fold {
        df.withColumn("hash", sha1(concat($"chromosome", $"start", $"reference", $"alternate")))
      }{_ =>
        df
      }

    dfWithId.saveToEs(s"$indexName/_doc", ES_config)
  }

  val esClient = new ElasticSearchClient(esNodes.split(',').head)
  Try {
    println(s"ElasticSearch 'isRunning' status: [${esClient.isRunning}]")
    println(s"ElasticSearch 'checkNodes' status: [${esClient.checkNodeRoles}]")

    //val respDelete = esClient.deleteIndex(s"${indexName}_$release")
    //println(s"DELETE INDEX[${indexName}_$release] : " + respDelete.getStatusLine.getStatusCode + " : " + respDelete.getStatusLine.getReasonPhrase)
  }
  val response = esClient.setTemplate(s"s3://kf-strides-variant-parquet-prd/jobs/templates/$templateFileName")
  println(s"SET TEMPLATE[${templateFileName}] : " + response.getStatusLine.getStatusCode + " : " + response.getStatusLine.getReasonPhrase)
  run(spark.read.json(input), s"${indexName}_$release")

}
