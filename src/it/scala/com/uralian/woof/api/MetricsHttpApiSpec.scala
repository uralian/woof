package com.uralian.woof.api

import com.uralian.woof.AbstractITSpec
import com.uralian.woof.api.metrics._
import com.uralian.woof.http.DataDogClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Metrics API test suite.
 */
class MetricsHttpApiSpec extends AbstractITSpec {

  implicit val serialization = org.json4s.native.Serialization

  val client = DataDogClient()
  val api: MetricsApi = new MetricsHttpApi(client)

  val ddTags = List[Tag]("xx" -> randomString(), "yy" -> randomString())

  "MetricsHttpApi" should {
    "post a new metric series" in {
      val now = currentTime()
      val minus5 = now minusSeconds 5
      val minus10 = now minusSeconds 10
      val series = CreateSeries("woof.test.metric", List[Point](now -> 5.5, minus5 -> 3.0, minus10 -> 1.5))
        .asCount(30 seconds)
        .withTags(ddTags: _*)
        .withHost(host)
      val rsp = api.createSeries(List(series)).futureValue
      rsp mustBe true
    }
    "query metric series" in {
      Thread.sleep(30000)
      val query = MetricQuery.metric("woof.test.metric")
        .aggregate(MetricAggregator.Max)
        .filterBy(ddTags: _*)
        .groupBy("host")
      val rsp = api.querySeries(query, currentTime() minusSeconds 3600, currentTime() plusSeconds 3600).futureValue
      val ts = rsp.headOption.value
      val allTags: Seq[Tag] = ("host" -> host) :: ddTags
      ts.metric mustBe "woof.test.metric"
      ts.tags mustBe Seq[Tag]("host" -> "uralian.test.host")
      ts.aggregation mustBe "max"
      inside(ts.scope) {
        case Scope.Filter(elements @ _*) => elements.toSet mustBe allTags.toSet
      }
      ts.displayName mustBe "woof.test.metric"
    }
    "retrieve all active metrics" in {
      val allMetrics = api.getActiveMetrics(currentTime().minusSeconds(3600 * 24 * 30)).futureValue
      allMetrics must not be empty
      val hostMetrics = api.getActiveMetrics(currentTime().minusSeconds(3600 * 24 * 30), Some(host)).futureValue
      hostMetrics must not be empty
    }
    "search for metrics" in {
      val metrics = api.searchMetrics("woof").futureValue
      metrics must contain("woof.test.metric")
    }
    "update metric metadata" in {
      val md = MetricMetadata(MetricType.Count, "w.t.m")
        .withDescription("Woof test metric")
        .withUnit("operation")
        .withPerUnit("minute")
        .withStatsDInterval(60 seconds)
      val rsp = api.updateMetadata("woof.test.metric", md).futureValue
      rsp mustBe md
    }
    "retrieve metric metadata" in {
      val md = api.getMetadata("woof.test.metric").futureValue
      md mustBe MetricMetadata(MetricType.Count, "w.t.m")
        .withDescription("Woof test metric")
        .withUnit("operation")
        .withPerUnit("minute")
        .withStatsDInterval(60 seconds)
    }
  }
}