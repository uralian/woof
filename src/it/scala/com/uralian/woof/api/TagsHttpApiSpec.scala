package com.uralian.woof.api

import com.typesafe.config.ConfigFactory
import com.uralian.woof.AbstractITSpec
import com.uralian.woof.api.tags.TagsHttpApi
import com.uralian.woof.http.DataDogClient

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Tags API test suite.
 */
class TagsHttpApiSpec extends AbstractITSpec {

  val config = ConfigFactory.load("integration.conf").getConfig("tags")
  val host = config.getString("host.name")
  val defaultTags = config.getStringList("host.tags").asScala.map(Tag.apply).toSet

  implicit val serialization = org.json4s.native.Serialization

  val client = DataDogClient()
  val api = new TagsHttpApi(client)

  "TagsHttpApi" should {
    "retrieve all infrastructure tags" in {
      val allTags = api.getAll().futureValue
      val tagsForHost = allTags.filter {
        case (tag, hosts) => hosts contains host
      }.keys
      tagsForHost must contain allElementsOf defaultTags
    }
    "retrieve tags for a host" in {
      val tags = (for {
        _ <- api.remove(host)
        tags <- api.get(host)
      } yield tags).futureValue
      tags.toSet mustBe defaultTags
    }
    "add host tags" in {
      val (aTags, afterATags, bTags, allTags) = (for {
        aTags <- api.add(host, None, "testA" -> "AA")
        afterATags <- api.get(host)
        bTags <- api.add(host, None, "testB" -> "BB")
        allTags <- api.get(host)
      } yield (aTags, afterATags, bTags, allTags)).futureValue
      aTags.toSet mustBe Set(Tag("testa", "aa"))
      afterATags.toSet mustBe defaultTags ++ aTags
      bTags.toSet mustBe Set(Tag("testa", "aa"), Tag("testb", "bb"))
      allTags.toSet mustBe defaultTags ++ aTags ++ bTags
    }
    "update host tags" in {
      val (newTags, allTags) = (for {
        newTags <- api.update(host, None, "testA" -> "XY", "testC" -> "XXYY")
        allTags <- api.get(host)
      } yield (newTags, allTags)).futureValue
      newTags.toSet mustBe Set(Tag("testa", "xy"), Tag("testc", "xxyy"))
      allTags.toSet mustBe defaultTags ++ newTags
    }
    "remove host tags" in {
      val tags = (for {
        _ <- api.remove(host)
        tags <- api.get(host)
      } yield tags).futureValue
      tags.toSet mustBe defaultTags
    }
  }
}
