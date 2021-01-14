package org.kidsfirstdrc

import org.kidsfirstdrc.utils.WithSparkSession
import org.kidsfirstdrc.variant.ElasticSearchClient
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class ElasticSearchClientSpec extends AnyFlatSpec with GivenWhenThen with WithSparkSession with Matchers {

  val indexName = "test"
  val templateFileName = "template.json"
  val templateName = "test_template"
  val esUrl = "http://localhost:9201" //assuming es 7 is on :9200 and es 6 on 9201
  val esClient = new ElasticSearchClient(esUrl)

  "ES instance" should "be up on localhost:9201" in {
    esClient.isRunning shouldBe true
  }

  "ES client" should "create index" in {
    esClient.createIndex(indexName).getStatusLine.getStatusCode shouldBe 200
  }

  "ES client" should "delete index" in {
    esClient.deleteIndex(indexName).getStatusLine.getStatusCode shouldBe 200
  }

  "ES client" should "create template" in {
    esClient.setTemplate(templateFileName, templateName).getStatusLine.getStatusCode shouldBe 200
  }

  "ES client" should "delete template" in {
    esClient.deleteTemplate(templateName).getStatusLine.getStatusCode shouldBe 200
  }

}
