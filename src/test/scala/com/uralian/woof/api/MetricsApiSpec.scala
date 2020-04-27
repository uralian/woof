package com.uralian.woof.api

import java.time.Instant

import com.uralian.woof.AbstractUnitSpec
import com.uralian.woof.api.metrics.MetricScale.{CountScale, GaugeScale, RateScale}
import com.uralian.woof.api.metrics._
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization

import scala.concurrent.duration._

/**
 * MetricsApi test suite.
 */
class MetricsApiSpec extends AbstractUnitSpec {

  import MetricType._

  "MetricMetadata" should {
    "serialize to JSON" in {
      val md = MetricMetadata(Gauge, "w.t.m")
        .withDescription("Woof test metric")
        .withUnit("apples")
        .withPerUnit("oranges")
        .withStatsDInterval(10 seconds)
      val json = Extraction.decompose(md)
      json mustBe ("type" -> "gauge") ~ ("short_name" -> "w.t.m") ~ ("description" -> "Woof test metric") ~
        ("unit" -> "apples") ~ ("per_unit" -> "oranges") ~ ("statsd_interval" -> JLong(10))
    }
    "deserialize from JSON" in {
      val json = parse(
        """
          |{
          |  "type": "rate", "short_name": "w.t.m", "description": "info", "unit": "operation",
          |  "per_unit": "minute"
          |}
          |""".stripMargin)
      val md = json.extract[MetricMetadata]
      md mustBe MetricMetadata(Rate, "w.t.m", Some("info"), Some("operation"), Some("minute"))
    }
    "render toString as JSON" in {
      val md = MetricMetadata(Gauge, "w.t.m")
        .withDescription("Woof test metric")
        .withUnit("apples")
        .withPerUnit("oranges")
        .withStatsDInterval(10 seconds)
      Serialization.read[MetricMetadata](md.toString) mustBe md
    }
  }

  "UnitInfo" should {
    "deserialize from JSON" in {
      val json =
        """
          |{
          |  "family": "general",
          |  "scale_factor": 1.0,
          |  "name": "operation",
          |  "short_name": "op",
          |  "plural": "operations",
          |  "id": 57
          |}
          |""".stripMargin
      val ui = Serialization.read[UnitInfo](json)
      ui mustBe UnitInfo("general", 1.0, "operation", "op", "operations", 57)
    }
    "render toString as JSON" in {
      val ui = UnitInfo("general", 1.0, "operation", "op", "operations", 57)
      Serialization.read[UnitInfo](ui.toString) mustBe ui
    }
  }

  "MetricScale" should {
    "serialize to JSON" in {
      Extraction.decompose(CountScale(Some(30 seconds))) mustBe ("type" -> "count") ~ ("interval" -> JLong(30))
      Extraction.decompose(RateScale(None)) mustBe ("type" -> "rate") ~ ("interval" -> JNothing)
      Extraction.decompose(GaugeScale) mustBe JObject("type" -> JString("gauge"))
    }
  }

  "Point" should {
    "serialize to JSON" in {
      val now = currentTime()
      val seconds = JLong(now.getEpochSecond)
      Extraction.decompose(Point(now, 123.45)) mustBe JArray(List(seconds, JDecimal(123.45)))
      Extraction.decompose(Point(now, 123)) mustBe JArray(List(seconds, JDecimal(123)))
    }
    "deserialize from JSON" in {
      Serialization.read[Point]("[12345678000, 0.5]") mustBe Point(12345678, 0.5)
      Serialization.read[Point]("[12345678000, 100]") mustBe Point(12345678, 100)
    }
  }

  "CreateSeries" should {
    val now = currentTime()
    val minus5 = now minusSeconds 5
    val series = CreateSeries("woof.test.metric", List[Point](now -> 12.34, minus5 -> 0.5))
      .asRate(30 seconds)
      .withHost("uralian.test.host")
      .withTags("a" -> "b", "c" -> "d")
    "serialize to JSON" in {
      val json = Extraction.decompose(series)
      json mustBe ("metric" -> "woof.test.metric") ~
        ("points" -> List(
          List[JValue](Extraction.decompose(now), JDecimal(12.34)),
          List[JValue](Extraction.decompose(minus5), JDecimal(0.5)))) ~
        ("tags" -> List("a:b", "c:d")) ~
        ("host" -> "uralian.test.host") ~
        ("type" -> "rate") ~
        ("interval" -> JLong(30))
    }
  }

  "Timeseries" should {
    "deserialize from JSON" in {
      val json =
        """
          |{
          |  "end": 1586822399000,
          |  "attributes": {},
          |  "metric": "woof.test.metric",
          |  "interval": 43200,
          |  "tag_set": [
          |    "host:uralian.test.host"
          |  ],
          |  "start": 1586779200000,
          |  "length": 1,
          |  "query_index": 0,
          |  "aggr": "avg",
          |  "scope": "host:uralian.test.host",
          |  "pointlist": [
          |     [
          |       1586779200000.0,
          |       10.535000006357828
          |     ]
          |  ],
          |  "expression": "avg:woof.test.metric{host:uralian.test.host}",
          |  "unit": [
          |    {
          |      "family": "general",
          |      "scale_factor": 1.0,
          |      "name": "operation",
          |      "short_name": "op",
          |      "plural": "operations",
          |      "id": 57
          |    }
          |  ],
          |  "display_name": "woof.test.metric"
          |}
          |""".stripMargin
      val ts = Serialization.read[Timeseries](json)
      ts mustBe Timeseries("woof.test.metric",
        Instant.ofEpochMilli(1586779200000L),
        Instant.ofEpochMilli(1586822399000L),
        43200 seconds, Seq("host" -> "uralian.test.host"), 1, 0, "avg",
        Scope.Filter("host" -> "uralian.test.host"),
        Seq(Instant.ofEpochMilli(1586779200000L) -> 10.535000006357828),
        "avg:woof.test.metric{host:uralian.test.host}",
        Seq(UnitInfo("general", 1.0, "operation", "op", "operations", 57)),
        "woof.test.metric"
      )
    }
  }
}