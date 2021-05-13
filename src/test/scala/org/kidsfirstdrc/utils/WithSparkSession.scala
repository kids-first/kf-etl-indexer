package org.kidsfirstdrc.utils

import org.apache.commons.io.FileUtils
import org.apache.spark.sql.SparkSession

import java.io.File
import java.nio.file.{Files, Path}

trait WithSparkSession {

  private val tmp = new File("tmp").getAbsolutePath
  implicit lazy val spark: SparkSession = SparkSession.builder()
    .config("spark.ui.enabled", value = false)
    .config("spark.sql.warehouse.dir", s"$tmp/wharehouse")
    .config("spark.driver.extraJavaOptions", s"-Dderby.system.home=$tmp/derby")
    .enableHiveSupport()
    .master("local")
    .getOrCreate()


  def withOutputFolder[T](prefix: String)(block: String => T): T = {
    val output: Path = Files.createTempDirectory(prefix)
    try {
      block(output.toAbsolutePath.toString)
    } finally {
      FileUtils.deleteDirectory(output.toFile)
    }
  }

  def withEsIndex[T](indexName: String)(block: String => T): T = {
    val esClient = new ElasticSearchClient("http://localhost:9200")
    esClient.createIndex(indexName)
    val indexUrl = s"http://localhost:9200/$indexName"
    try {
      block(indexUrl)
    } finally {
      esClient.deleteIndex(indexName)
    }
  }
}
