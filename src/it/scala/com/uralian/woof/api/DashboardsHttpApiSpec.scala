package com.uralian.woof.api

import com.uralian.woof.AbstractITSpec
import com.uralian.woof.api.MetricQuery._
import com.uralian.woof.api.dashboards._
import com.uralian.woof.api.dsl._
import com.uralian.woof.api.graphs.ColorPalette.Cool
import com.uralian.woof.api.graphs.GraphScale.Sqrt
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
    "delete dashboards" in {
      dashboardIds map api.delete foreach { rsp =>
        rsp.futureValue mustBe true
      }
    }
  }
}
