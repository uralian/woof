package com.uralian.woof.api

import com.uralian.woof.AbstractITSpec
import com.uralian.woof.api.MetricQuery._
import com.uralian.woof.api.SortDirection.Descending
import com.uralian.woof.api.dashboards._
import com.uralian.woof.api.dsl._
import com.uralian.woof.api.graphs.ColorPalette.Cool
import com.uralian.woof.api.graphs.FormatColor.{Green, Red, White}
import com.uralian.woof.api.graphs.FormatComparator.GE
import com.uralian.woof.api.graphs.GraphScale.{Log, Sqrt}
import com.uralian.woof.api.graphs.HostmapPalette.YellowGreen
import com.uralian.woof.api.graphs.QueryValueAggregator.Last
import com.uralian.woof.api.graphs.RankAggregator.Mean
import com.uralian.woof.api.graphs.TextAlign.Left
import com.uralian.woof.api.graphs._
import com.uralian.woof.http.DataDogClient

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Dashboards API test suite.
 */
class DashboardsHttpApiSpec extends AbstractITSpec {

  implicit val serialization = org.json4s.native.Serialization

  val client = DataDogClient()
  val api: DashboardsApi = new DashboardsHttpApi(client)

  var dashboardIds: List[String] = Nil
  var dashboardListId: Int = _

  "DashboardsHttpApi" should {
    "create Timeseries widgets" in {
      import Visualization.Timeseries._
      val w1 = Widget.Ordered(graph(
        plot(direct("avg:system.cpu.user{*}").as("usr")).displayAs(DisplayType.Bars),
        plot(direct("avg:system.cpu.system{*}").as("sys"))
      ).withYAxis(AxisOptions().withMin(1).withScale(GraphScale.Log)).withTitle("graph1"))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
        .withDescription("Sample dashboard")
        .notifyUsers("user@test.com")
        .withVars(
          TemplateVar("host").withPrefix("host").withDefault("uralian.test.host"),
          TemplateVar("env").withPrefix("env")
        )
        .withPresets(Preset("qa").withVars("env" -> "qa"))
      val rsp = api.create(request).futureValue
      rsp.id must not be null
      rsp.title mustBe "Sample"
      rsp.url must not be null
      rsp.description.value mustBe "Sample dashboard"
      rsp.createdAt must not be null
      rsp.modifiedAt must not be null
      rsp.isReadOnly mustBe false
      rsp.layoutType mustBe LayoutType.Ordered
      rsp.notifyList mustBe empty
      rsp.templateVars mustBe Seq(TemplateVar("host", Some("host"), Some(host)), TemplateVar("env", Some("env")))
      rsp.presets mustBe Seq(Preset("qa", Map("env" -> "qa")))
      dashboardIds +:= rsp.id
    }
    "create Change widgets" in {
      import Visualization.Change._
      val w1 = Widget.Ordered(graph(
        plot("system.load.1").aggregate(MetricAggregator.Sum).filterBy("env" -> "staging")
          .groupBy("host").compareTo(TimeBase.DayBefore)
          .sortBy(ChangeOrder.Change, SortDirection.Descending).showPresent
      ).withTitle("graph2"))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
      val rsp = api.create(request).futureValue
      rsp.id must not be null
      rsp.title mustBe "Sample"
      rsp.url must not be null
      rsp.description mustBe empty
      dashboardIds +:= rsp.id
    }
    "create Distribution widgets" in {
      import Visualization.Distribution._
      val w1 = Widget.Ordered(graph(plot(metric("system.cpu.user").aggregate(MetricAggregator.Avg)
        .filterBy("env" -> "qa").groupBy("host"),
        direct("avg:system.cpu.user{env:qa} by {host}/2")
      ).withPalette(ColorPalette.Cool)).withTitle("graph2"))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
      val rsp = api.create(request).futureValue
      rsp.id must not be null
      rsp.title mustBe "Sample"
      rsp.url must not be null
      rsp.description mustBe empty
      dashboardIds +:= rsp.id
    }
    "create Heatmap widgets" in {
      import Visualization.Heatmap._
      val w1 = Widget.Ordered(graph(plot(
        direct("avg:system.cpu.user{*}by{env}"), direct("avg:system.cpu.idle{*}by{env}")
      ).withPalette(Cool)).withYAxis(AxisOptions(scale = Sqrt, includeZero = false)))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
      val rsp = api.create(request).futureValue
      rsp.id must not be null
      rsp.title mustBe "Sample"
      rsp.url must not be null
      rsp.description mustBe empty
      dashboardIds +:= rsp.id
    }
    "create Hostmap widgets" in {
      import Visualization.Hostmap._
      val w1 = Widget.Ordered(graph(
        "system.cpu.user" -> MetricAggregator.Max, "system.mem.total" -> MetricAggregator.Avg)
        .withPalette(YellowGreen)
        .withMin(5)
        .filterBy("env:qa")
        .groupBy("region")
        .hideNoMetricHosts
        .hideNoGroupHosts
        .flipped
      )
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
      val rsp = api.create(request).futureValue
      rsp.id must not be null
      rsp.title mustBe "Sample"
      rsp.url must not be null
      rsp.description mustBe empty
      dashboardIds +:= rsp.id
    }
    "create QueryValue widgets" in {
      import Visualization.QueryValue._
      val w1 = Widget.Ordered(graph(plot(metric("system.cpu.user").groupBy("host"))
        .aggregate(Last)
        .withFormats(ConditionalFormat(GE, 0).withStandardColors(Green on White))
      ).withCustomUnit("mmm").withPrecision(2).withAlign(Left))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
      val rsp = api.create(request).futureValue
      rsp.id must not be null
      rsp.title mustBe "Sample"
      rsp.url must not be null
      rsp.description mustBe empty
      dashboardIds +:= rsp.id
    }
    "create Scatterplot widgets" in {
      import graphs.Visualization.Scatter._
      val w1 = Widget.Ordered(graph(
        axis("system.mem.used").aggregate(MetricAggregator.Max).withOptions(label = Some("xxx")),
        axis("system.load.1").rollup(QueryValueAggregator.Sum).withOptions(scale = Log, max = Some(100))
      ).pointBy("client").colorBy("env"))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
      val rsp = api.create(request).futureValue
      rsp.id must not be null
      rsp.title mustBe "Sample"
      rsp.url must not be null
      rsp.description mustBe empty
      dashboardIds +:= rsp.id
    }
    "create QueryTable widgets" in {
      import graphs.Visualization.QueryTable._
      val w1 = Widget.Ordered(graph(
        column("system.cpu.user"), column("system.mem.used"), column("system.load.1")
      ).groupBy("host").withKeyColumn(1))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
      val rsp = api.create(request).futureValue
      rsp.id must not be null
      rsp.title mustBe "Sample"
      rsp.url must not be null
      rsp.description mustBe empty
      dashboardIds +:= rsp.id
    }
    "create Toplist widgets" in {
      import graphs.Visualization.Toplist._
      val w1 = Widget.Ordered(graph(plot(direct("avg:system.cpu.user{*}by{host}"))
        .withRows(Descending, 5).aggregate(Mean).withFormats(ConditionalFormat(GE, 90, White on Red))))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
      val rsp = api.create(request).futureValue
      rsp.id must not be null
      rsp.title mustBe "Sample"
      rsp.url must not be null
      rsp.description mustBe empty
      dashboardIds +:= rsp.id
    }
    "create Group widgets" in {
      val w1 = Widget.Ordered(Visualization.Heatmap.graph(Visualization.Heatmap.plot(
        direct("avg:system.cpu.user{*}by{env}"), direct("avg:system.cpu.idle{*}by{env}")
      ).withPalette(Cool)).withYAxis(AxisOptions(scale = Sqrt, includeZero = false)))
      val w2 = Widget.Ordered(Visualization.Distribution.graph(Visualization.Distribution.plot(
        metric("system.cpu.user").aggregate(MetricAggregator.Avg)
          .filterBy("env" -> "qa").groupBy("host"),
        direct("avg:system.cpu.user{env:qa} by {host}/2")
      ).withPalette(ColorPalette.Cool)).withTitle("graph2"))
      val w3 = Widget.Ordered(Visualization.Change.graph(
        Visualization.Change.plot("system.load.1").aggregate(MetricAggregator.Sum)
          .filterBy("env" -> "staging")
          .groupBy("host").compareTo(TimeBase.DayBefore)
          .sortBy(ChangeOrder.Change, SortDirection.Descending).showPresent
      ).withTitle("graph2"))
      val w = Widget.Ordered(WidgetGroup(List(w1, w2, w3)).withTitle("my_group"))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w))
      val rsp = api.create(request).futureValue
      rsp.id must not be null
      rsp.title mustBe "Sample"
      rsp.url must not be null
      rsp.description mustBe empty
      dashboardIds +:= rsp.id
    }
    "retrieve dashboard by id" in {
      val rsp = api.get(dashboardIds.head).futureValue
      rsp.id mustBe dashboardIds.head
    }
    "retrieve all dashboards" in {
      val rsp = api.getAll().futureValue
      rsp.map(_.id) must contain allElementsOf dashboardIds
    }
    "update a dashboard" in {
      import Visualization.Change._
      val w1 = Widget.Ordered(graph(
        plot("system.load.1").aggregate(MetricAggregator.Sum).filterBy("env" -> "staging")
          .groupBy("host").compareTo(TimeBase.DayBefore)
          .sortBy(ChangeOrder.Change, SortDirection.Descending).showPresent
      ).withTitle("graph2"))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
      val rsp = api.update(dashboardIds.head, request).futureValue
      rsp.id mustBe dashboardIds.head
      rsp.title mustBe "Sample"
      rsp.url must not be null
      rsp.description mustBe empty
    }
    "create a dashboard list" in {
      val rsp = api.createList("Sample DL").futureValue
      dashboardListId = rsp.id
      rsp.id must not be 0
      rsp.name mustBe "Sample DL"
    }
    "retrieve dashboard list by id" in {
      val rsp = api.getList(dashboardListId).futureValue
      rsp.id mustBe dashboardListId
      rsp.name mustBe "Sample DL"
    }
    "retrieve all dashboard lists" in {
      val rsp = api.getAllLists.futureValue
      rsp must not be empty
      rsp.map(_.name) must contain("Sample DL")
    }
    "update dashboard list" in {
      val rsp = api.updateList(dashboardListId, "New Sample DL").futureValue
      rsp.id mustBe dashboardListId
      rsp.name mustBe "New Sample DL"
    }
    "delete dashboard list" in {
      val rsp = api.deleteList(dashboardListId).futureValue
      rsp mustBe true
    }
    "delete dashboards" in {
      dashboardIds map api.delete foreach { rsp =>
        rsp.futureValue mustBe true
      }
    }
  }
}
