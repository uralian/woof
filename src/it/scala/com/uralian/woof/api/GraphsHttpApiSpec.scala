package com.uralian.woof.api

import com.uralian.woof.AbstractITSpec
import com.uralian.woof.api.graphs._
import com.uralian.woof.http.DataDogClient

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Graphs API test suite.
 */
class GraphsHttpApiSpec extends AbstractITSpec {

  import ColorPalette._
  import DisplayType._
  import GraphDSL._
  import LineType._
  import MetricQuery._
  import Stroke._

  implicit val serialization = org.json4s.native.Serialization

  val client = DataDogClient()
  val api: GraphsApi = new GraphsHttpApi(client)

  var graphIds: List[String] = Nil
  
  "GraphsHttpApi" should {
    "create Timeseries graph" in {
      val graphDefinition = timeseries(
        plot(
          metric("system.cpu.user").groupBy("host").as("a1"),
          metric("system.cpu.idle").groupBy("host")
        ).displayAs(Bars).withPalette(Warm),
        plot(
          metric("system.cpu.system").groupBy("host").as("a2")
        ).displayAs(Area).withStyle(Warm, Dashed, Thick),
        plot(
          text("avg:system.cpu.user{*}by{host}").as("a3")
        ).displayAs(Line)
      )
      val request = CreateGraph(graphDefinition)
        .withTitle("Sample Graph")
        .withTimeframe(Timeframe.Hour4)
        .withSize(GraphSize.Small)
        .withLegend
      val rsp = api.create(request).futureValue
      rsp.id must not be empty
      rsp.templateVariables mustBe empty
      rsp.html must (include("legend=true") and include("""width="400""""))
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
      graphs.size must be >= 1
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