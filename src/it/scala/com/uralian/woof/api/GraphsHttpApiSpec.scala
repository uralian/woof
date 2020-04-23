package com.uralian.woof.api

import com.uralian.woof.AbstractITSpec
import com.uralian.woof.api.graphs.GraphSize.{Medium, Small, XLarge}
import com.uralian.woof.api.graphs._
import com.uralian.woof.http.DataDogClient

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Graphs API test suite.
 */
class GraphsHttpApiSpec extends AbstractITSpec {

  implicit val serialization = org.json4s.native.Serialization

  val client = DataDogClient()
  val api: GraphsApi = new GraphsHttpApi(client)

  var graphIds: List[String] = Nil

  "GraphsHttpApi" should {
    "create a single query graph" in {
      val request = CreateGraph("avg:system.cpu.user{*}")
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
      graphIds +:= rsp.id
    }
    "create a multi-query graph" in {
      val request = CreateGraph("avg:system.cpu.user{*}")
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
      graphIds +:= rsp.id
    }
    "create a stack graph" in {
      val request = CreateGraph("avg:system.cpu.user{*}, avg:system.cpu.system{*}")
        .withTitle("Sample Graph")
        .withTimeframe(Timeframe.Hour1)
        .withSize(Medium)
      val rsp = api.create(request).futureValue
      rsp.id must not be empty
      rsp.templateVariables mustBe empty
      rsp.html must (not include ("legend=true") and include("""width="600""""))
      rsp.title mustBe "Sample Graph"
      rsp.revoked mustBe false
      graphIds +:= rsp.id
    }
    "create a graph with variables" in {
      val request = CreateGraph("avg:system.cpu.user{$var}")
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
      graphIds +:= rsp.id
    }
    "retrieve a graph for valid id" in {
      val graph = api.get(graphIds.head).futureValue
      graph.id mustBe graphIds.head
      graph.revoked mustBe false
    }
    "fail to retrieve a graph for invalid id" in {
      api.get("11111111111111111111111111").failed.futureValue mustBe a[DataDogApiError]
    }
    "retrieve all graphs" in {
      val graphs = api.getAll.futureValue
      graphs.size must be >= 4
      graphs.map(_.id) must contain allElementsOf (graphIds)
    }
    "enable graphs" in {
      api.enable(graphIds.head).futureValue mustBe true
    }
    "revoke graphs" in {
      graphIds map api.revoke foreach { rsp =>
        rsp.futureValue mustBe true
      }
    }
  }
}