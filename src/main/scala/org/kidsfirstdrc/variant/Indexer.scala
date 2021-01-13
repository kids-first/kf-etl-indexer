package org.kidsfirstdrc.variant

import org.apache.spark.sql.functions.{concat, sha1}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.elasticsearch.spark.sql._

import scala.util.Try

object Indexer extends App {

  val Array(input, esNodes, indexName, release, templateFileName) = args
  implicit val spark: SparkSession = SparkSession.builder
    .config("es.index.auto.create", "true")
    .config("es.nodes", esNodes)
    .config("es.wan.only", true)
    .appName(s"Indexer").getOrCreate()

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
