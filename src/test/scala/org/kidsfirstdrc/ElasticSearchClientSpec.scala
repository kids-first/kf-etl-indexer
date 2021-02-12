package org.kidsfirstdrc

import org.kidsfirstdrc.utils.WithSparkSession
import org.kidsfirstdrc.variant.ElasticSearchClient
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import scala.util.Random

class ElasticSearchClientSpec extends AnyFlatSpec with GivenWhenThen with WithSparkSession with Matchers {

  val indexName = Random.nextString(20)
  val templateFileName = getClass.getClassLoader.getResource("template_example.json").getFile
  val esUrl = "http://localhost:9200"
  //qa testing: val esUrl = "https://vpc-kf-arranger-blue-es-service-exwupkrf4dyupg24dnfmvzcwri.us-east-1.es.amazonaws.com:443"
  val esClient = new ElasticSearchClient(esUrl)

  "ES instance" should s"be up on $esUrl" in {
    esClient.isRunning shouldBe true
  }

  "ES client" should "create then delete index" in {
    esClient.createIndex(indexName).getStatusLine.getStatusCode shouldBe 200
    esClient.deleteIndex(indexName).getStatusLine.getStatusCode shouldBe 200
  }

  "ES client" should "create then delete template" in {
    esClient.setTemplate(templateFileName).getStatusLine.getStatusCode shouldBe 200
    esClient.deleteTemplate("template_example").getStatusLine.getStatusCode shouldBe 200
  }

}
