package org.kidsfirstdrc

import org.kidsfirstdrc.utils.Models.TestDocs
import org.kidsfirstdrc.utils.WithSparkSession
import org.kidsfirstdrc.variant.Indexer
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class IndexerSpec extends AnyFlatSpec with GivenWhenThen with WithSparkSession with Matchers {

  import spark.implicits._

  val indexName = "test"

  "indexer" should "push data to es" in {
    withEsIndex("test"){indexUrl =>
      val path = this.getClass.getResource("/test.json").getFile

      val df = spark.read.json(path)

      Indexer.run(df, "test")

      spark.read.format("es").load("test").as[TestDocs].collect() should contain theSameElementsAs Seq(
        TestDocs("id1", "value1"),
        TestDocs("id2", "value2")
      )
    }
  }

}
