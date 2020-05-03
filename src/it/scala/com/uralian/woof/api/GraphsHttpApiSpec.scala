package com.uralian.woof.api

import com.uralian.woof.AbstractITSpec
import com.uralian.woof.api.graphs.ChangeOrder.Change
import com.uralian.woof.api.graphs.HostmapPalette.YellowGreen
import com.uralian.woof.api.graphs.TimeBase.DayBefore
import com.uralian.woof.api.graphs._
import com.uralian.woof.http.DataDogClient

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Graphs API test suite.
 * - not testing QueryTable as it is not supported for embeddable graphs
 */
class GraphsHttpApiSpec extends AbstractITSpec {

  import ColorPalette._
  import DisplayType._
  import FormatColor._
  import FormatComparator._
  import GraphDSL._
  import GraphScale._
  import LineType._
  import MetricQuery._
  import QueryValueAggregator._
  import SortDirection._
  import Stroke._
  import TextAlign._

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
      val rsp = api.create(request).futureValue
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
      val rsp = api.create(request).futureValue
      rsp.id must not be empty
      rsp.templateVariables mustBe empty
      rsp.html must (include("legend=true") and include("""width="800""""))
      rsp.title mustBe "QueryValue Graph"
      rsp.revoked mustBe false
      graphIds +:= rsp.id
    }
    "create Heatmap graph" in {
      import Visualization.Heatmap._
      val g = graph(plot(text("avg:system.cpu.idle{*}by{host}"), text("avg:system.cpu.system{$var}by{host}")
      ).withPalette(Cool)).withYAxis(scale = Sqrt, includeZero = false)
      val request = CreateGraph(g)
        .withTitle("Heatmap Graph")
        .withTimeframe(Timeframe.Hour4)
        .withSize(GraphSize.Large)
        .withLegend
      val rsp = api.create(request).futureValue
      rsp.id must not be empty
      rsp.templateVariables mustBe Seq("var")
      rsp.html must (include("legend=true") and include("""width="800""""))
      rsp.title mustBe "Heatmap Graph"
      rsp.revoked mustBe false
      graphIds +:= rsp.id
    }
    "create a Scatterplot graph" in {
      import Visualization.Scatter._
      val g = graph(
        axis("system.cpu.user").aggregate(MetricAggregator.Min).withOptions(label = Some("xxx")),
        axis("system.cpu.idle").rollup(QueryValueAggregator.Max).withOptions(scale = Log, max = Some(100))
      ).pointBy("env").colorBy("client")
      val request = CreateGraph(g)
        .withTitle("Scatter Graph")
        .withTimeframe(Timeframe.Hour4)
        .withSize(GraphSize.Large)
        .withLegend
      val rsp = api.create(request).futureValue
      rsp.id must not be empty
      rsp.templateVariables mustBe Seq("var")
      rsp.html must (include("legend=true") and include("""width="800""""))
      rsp.title mustBe "Scatter Graph"
      rsp.revoked mustBe false
      graphIds +:= rsp.id
    }
    "create a Distribution graph" in {
      import Visualization.Distribution._
      val g = graph(plot(text("avg:system.cpu.user{env:qa} by {host}")).withPalette(Cool))
      val request = CreateGraph(g)
        .withTitle("Distribution Graph")
        .withTimeframe(Timeframe.Hour4)
        .withSize(GraphSize.Large)
        .withLegend
      val rsp = api.create(request).futureValue
      rsp.id must not be empty
      rsp.templateVariables mustBe empty
      rsp.html must (include("legend=true") and include("""width="800""""))
      rsp.title mustBe "Distribution Graph"
      rsp.revoked mustBe false
      graphIds +:= rsp.id
    }
    "create a Toplist graph" in {
      import Visualization.Toplist._
      val g = graph(plot(text("avg:system.cpu.user{env:production}by{host}"))
        .withRows(Descending, 20).aggregate(RankAggregator.Mean)
        .withFormats(ConditionalFormat(GT, 123, Red on White)))
      val request = CreateGraph(g)
        .withTitle("Toplist Graph")
        .withTimeframe(Timeframe.Hour4)
        .withSize(GraphSize.Large)
        .withLegend
      val rsp = api.create(request).futureValue
      rsp.id must not be empty
      rsp.templateVariables mustBe empty
      rsp.html must (include("legend=true") and include("""width="800""""))
      rsp.title mustBe "Toplist Graph"
      rsp.revoked mustBe false
      graphIds +:= rsp.id
    }
    "create a Change graph" in {
      import Visualization.Change._
      val g = graph(plot("system.load.1").aggregate(MetricAggregator.Max)
        .filterBy("env" -> "production").groupBy("host").compareTo(DayBefore).sortBy(Change, Descending)
        .increaseIsBetter.showAbsolute.showPresent)
      val request = CreateGraph(g)
        .withTitle("Change Graph")
        .withTimeframe(Timeframe.Hour4)
        .withSize(GraphSize.Large)
        .withLegend
      val rsp = api.create(request).futureValue
      rsp.id must not be empty
      rsp.templateVariables mustBe empty
      rsp.html must (include("legend=true") and include("""width="800""""))
      rsp.title mustBe "Change Graph"
      rsp.revoked mustBe false
      graphIds +:= rsp.id
    }
    "create a Hostmap graph" in {
      import Visualization.Hostmap._
      val g = graph("system.load.1" -> MetricAggregator.Max, "system.mem.used" -> MetricAggregator.Sum)
        .withPalette(YellowGreen).groupBy("env").hideNoMetricHosts
      val request = CreateGraph(g)
        .withTitle("Hostmap Graph")
        .withTimeframe(Timeframe.Hour4)
        .withSize(GraphSize.Large)
        .withLegend
      val rsp = api.create(request).futureValue
      rsp.id must not be empty
      rsp.templateVariables mustBe empty
      rsp.html must (include("legend=true") and include("""width="800""""))
      rsp.title mustBe "Hostmap Graph"
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