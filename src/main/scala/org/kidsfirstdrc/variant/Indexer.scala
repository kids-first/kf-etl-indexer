package org.kidsfirstdrc.variant

import org.apache.spark.sql.functions.{concat, sha1}
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

  val Array(input, esNodes, indexName, release, templateFileName) = args

  def setTemplate(esNodes: String, templateFileName: String): Unit = {
    Try {
      val esClient = new ElasticSearchClient(esNodes.split(',').head)
      val response = esClient.setTemplate(templateFileName, templateFileName.split('.').head)
      println(response.getStatusLine.getStatusCode + " : " + response.getStatusLine.getReasonPhrase)
    }
  }

  def run(df: DataFrame, indexName: String): Unit = {
    import spark.implicits._

    val dfWithId =
      df.columns.find(_.equals("id")).fold {
        df.withColumn("id", sha1(concat($"chromosome", $"start", $"reference", $"alternate")))
      }{_ =>
        df
      }

    dfWithId.saveToEs(s"$indexName/_doc", Map("es.mapping.id" -> "id"))
  }

  setTemplate(esNodes, templateFileName)
  run(spark.read.json(input), s"${indexName}_$release")


}
