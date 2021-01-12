package org.kidsfirstdrc.variant

import org.apache.spark.sql.functions.{concat, sha1}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.elasticsearch.spark.sql._

object Indexer extends App {

  val Array(input, esNodes, indexName, release) = args
  implicit val spark: SparkSession = SparkSession.builder
    .config("es.index.auto.create", "true")
    .config("es.nodes", esNodes)
    .appName(s"Indexer").getOrCreate()

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

  run(spark.read.json(input), s"${indexName}_$release")


}
