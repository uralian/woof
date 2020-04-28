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
  import FormatColor._
  import FormatComparator._
  import TextAlign._
  import QueryValueAggregator._

  implicit val serialization = org.json4s.native.Serialization

  val client = DataDogClient()
  val api: GraphsApi = new GraphsHttpApi(client)

  var graphIds: List[String] = Nil

  "GraphsHttpApi" should {
    "create Timeseries graph" in {
      import Visualization.Timeseries._
      val g = graph(
        plot(
          metric("system.mem.used").groupBy("host").as("a"),
          metric("system.mem.free").groupBy("host")
        ).displayAs(Bars).withPalette(Warm),
        plot(
          metric("system.mem.total").groupBy("host").as("b")
        ).displayAs(Area).withStyle(Warm, Dashed, Thick),
        plot(
          text("avg:system.cpu.user{*}by{host}").as("c")
        ).displayAs(Line)
      )
      val request = CreateGraph(g)
        .withTitle("Timeseries Graph")
        .withTimeframe(Timeframe.Hour4)
        .withSize(GraphSize.Large)
        .withLegend
      println(request)
      val rsp = api.create(request).futureValue
      println(rsp)
      rsp.id must not be empty
      rsp.templateVariables mustBe empty
      rsp.html must (include("legend=true") and include("""width="800""""))
      rsp.title mustBe "Timeseries Graph"
      rsp.revoked mustBe false
      graphIds +:= rsp.id
    }
    "create QueryValue graph" in {
      import Visualization.QueryValue._
      val g = graph(plot(metric("system.cpu.user").groupBy("host")).aggregate(Last)
        .withFormats(ConditionalFormat(GE, 5).withStandardColors(Green on Red))
      ).withCustomUnit("mmm").withPrecision(2).withAlign(Left)
      val request = CreateGraph(g)
        .withTitle("QueryValue Graph")
        .withTimeframe(Timeframe.Hour4)
        .withSize(GraphSize.Large)
        .withLegend
      println(request)
      val rsp = api.create(request).futureValue
      println(rsp)
      rsp.id must not be empty
      rsp.templateVariables mustBe empty
      rsp.html must (include("legend=true") and include("""width="800""""))
      rsp.title mustBe "QueryValue Graph"
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