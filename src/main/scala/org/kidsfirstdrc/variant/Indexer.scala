package org.kidsfirstdrc.variant

import org.apache.spark.sql.functions.{concat, sha1}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.elasticsearch.spark.sql._

import scala.util.Try

object Indexer extends App {

  implicit val spark: SparkSession = SparkSession.builder
    .config("es.index.auto.create", "true")
    .config("es.nodes", args(1))
    .config("es.wan.only", "true")
    .appName(s"Indexer").getOrCreate()
  spark.sparkContext.setLogLevel("ERROR")

  println(s"ARGS: " + args.mkString("[", ", ", "]"))

  val Array(input, esNodes, indexName, release, templateFileName) = args

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

  Try {
    val esClient = new ElasticSearchClient(esNodes.split(',').head)
    val respDelete = esClient.deleteIndex(indexName)
    println(s"DELETE INDEX[${indexName}] : " + respDelete.getStatusLine.getStatusCode + " : " + respDelete.getStatusLine.getReasonPhrase)
    val response = esClient.setTemplate(s"$templateFileName.json", templateFileName.split('.').head)
    println(s"SET TEMPLATE[${templateFileName}] : " + response.getStatusLine.getStatusCode + " : " + response.getStatusLine.getReasonPhrase)
  }
  run(spark.read.json(input), s"${indexName}_$release")


}
