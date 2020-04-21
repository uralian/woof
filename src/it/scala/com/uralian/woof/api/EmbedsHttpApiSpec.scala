package com.uralian.woof.api

import com.uralian.woof.AbstractITSpec
import com.uralian.woof.api.embed.GraphSize.{Medium, Small, XLarge}
import com.uralian.woof.api.embed._
import com.uralian.woof.http.DataDogClient

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Embeds API test suite.
 */
class EmbedsHttpApiSpec extends AbstractITSpec {

  implicit val serialization = org.json4s.native.Serialization

  val client = DataDogClient()
  val api: EmbedsApi = new EmbedsHttpApi(client)

  var embedIds: List[String] = Nil

  "EmbedsHttpApi" should {
    "create a single query graph" in {
      val request = CreateEmbed("avg:system.cpu.user{*}")
        .withTitle("Sample Graph")
        .withTimeframe(Timeframe.Hour4)
        .withSize(XLarge)
        .withLegend
      val rsp = api.create(request).futureValue
      rsp.id must not be empty
      rsp.templateVariables mustBe empty
      rsp.html must (include("legend=true") and include("""width="1000""""))
      rsp.title mustBe "Sample Graph"
      rsp.revoked mustBe false
      embedIds +:= rsp.id
    }
    "create a multi-query graph" in {
      val request = CreateEmbed("avg:system.cpu.user{*}")
        .withQueries("avg:system.cpu.idle{*}", "avg:system.cpu.system{*}")
        .withTitle("Sample Graph")
        .withTimeframe(Timeframe.Hour1)
        .withSize(Small)
      val rsp = api.create(request).futureValue
      rsp.id must not be empty
      rsp.templateVariables mustBe empty
      rsp.html must (not include ("legend=true") and include("""width="400""""))
      rsp.title mustBe "Sample Graph"
      rsp.revoked mustBe false
      embedIds +:= rsp.id
    }
    "create a stack graph" in {
      val request = CreateEmbed("avg:system.cpu.user{*}, avg:system.cpu.system{*}")
        .withTitle("Sample Graph")
        .withTimeframe(Timeframe.Hour1)
        .withSize(Medium)
      val rsp = api.create(request).futureValue
      rsp.id must not be empty
      rsp.templateVariables mustBe empty
      rsp.html must (not include ("legend=true") and include("""width="600""""))
      rsp.title mustBe "Sample Graph"
      rsp.revoked mustBe false
      embedIds +:= rsp.id
    }
    "create a graph with variables" in {
      val request = CreateEmbed("avg:system.cpu.user{$var}")
        .withTitle("Sample Graph")
        .withTimeframe(Timeframe.Hour4)
        .withSize(XLarge)
        .withLegend
      val rsp = api.create(request).futureValue
      rsp.id must not be empty
      rsp.templateVariables mustBe Seq("var")
      rsp.html must (include("legend=true") and include("""width="1000""""))
      rsp.title mustBe "Sample Graph"
      rsp.revoked mustBe false
      embedIds +:= rsp.id
    }
    "retrieve a graph for valid id" in {
      val graph = api.get(embedIds.head).futureValue
      graph.id mustBe embedIds.head
      graph.revoked mustBe false
    }
    "fail to retrieve a graph for invalid id" in {
      api.get("11111111111111111111111111").failed.futureValue mustBe a[DataDogApiError]
    }
    "retrieve all graphs" in {
      val graphs = api.getAll.futureValue
      graphs.size must be >= 4
      graphs.map(_.id) must contain allElementsOf (embedIds)
    }
    "enable graphs" in {
      api.enable(embedIds.head).futureValue mustBe true
    }
    "revoke graphs" in {
      embedIds map api.revoke foreach { rsp =>
        rsp.futureValue mustBe true
      }
    }
  }
}