package com.uralian.woof.api

import java.time.ZoneOffset

import com.uralian.woof.AbstractUnitSpec
import com.uralian.woof.api.dashboards._
import com.uralian.woof.api.dsl._
import com.uralian.woof.api.graphs.{AxisOptions, ChangeOrder, DisplayType, GraphScale, TimeBase, Visualization}
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.Serialization

/**
 * DashboardsApi test suite.
 */
class DashboardsApiSpec extends AbstractUnitSpec {

  import MetricQuery._

  "CreateDashboard" should {
    "produce a valid payload for Timeseries for Ordered layout" in {
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
      val json = Extraction.decompose(request)
      val jPresets: JValue = List(("name" -> "qa") ~ ("template_variables" -> List(("name" -> "env") ~ ("value" -> "qa"))))
      val jVars: JValue = List(
        ("name" -> "host") ~ ("prefix" -> "host") ~ ("default" -> "uralian.test.host"),
        ("name" -> "env") ~ ("prefix" -> "env")
      )
      val jPlots: JValue = List(
        ("q" -> "avg:system.cpu.user{*}") ~ ("display_type" -> "bars") ~ ("style" ->
          ("palette" -> "dog_classic") ~ ("line_type" -> "solid") ~ ("line_width" -> "normal")) ~
          ("metadata" -> List(("expression" -> "avg:system.cpu.user{*}") ~ ("alias_name" -> "usr"))),
        ("q" -> "avg:system.cpu.system{*}") ~ ("display_type" -> "line") ~ ("style" ->
          ("palette" -> "dog_classic") ~ ("line_type" -> "solid") ~ ("line_width" -> "normal")) ~
          ("metadata" -> List(("expression" -> "avg:system.cpu.system{*}") ~ ("alias_name" -> "sys")))
      )
      val jYaxis: JValue = ("scale" -> "log") ~ ("min" -> "1") ~ ("include_zero" -> true)
      val jWidget: JValue = ("definition" -> ("requests" -> jPlots) ~ ("yaxis" -> jYaxis) ~
        ("title" -> "graph1") ~ ("show_legend" -> false) ~ ("type" -> "timeseries"))
      json mustBe ("title" -> "Sample") ~ ("layout_type" -> "ordered") ~ ("widgets" -> List(jWidget)) ~
        ("description" -> "Sample dashboard") ~ ("is_read_only" -> false) ~ ("notify_list" -> List("user@test.com")) ~
        ("template_variables" -> jVars) ~ ("template_variable_presets" -> jPresets)
    }
    "produce a valid payload for Timeseries for free layout" in {
      import Visualization.Timeseries._
      val w1 = Widget.Free(
        graph(plot(direct("avg:system.load.1{*}by{env}").as("load"))).withLegend.withTitle("graph2"),
        Layout(1, 1, 50, 30)
      )
      val request = CreateDashboard("Sample", LayoutType.Free, Seq(w1))
        .withDescription("Sample dashboard")
        .notifyUsers("user@test.com")
        .withVars(
          TemplateVar("host").withPrefix("host").withDefault("uralian.test.host"),
          TemplateVar("env").withPrefix("env")
        )
        .withPresets(Preset("qa").withVars("env" -> "qa"))
      val json = Extraction.decompose(request)
      val jPresets: JValue = List(("name" -> "qa") ~ ("template_variables" -> List(("name" -> "env") ~ ("value" -> "qa"))))
      val jVars: JValue = List(
        ("name" -> "host") ~ ("prefix" -> "host") ~ ("default" -> "uralian.test.host"),
        ("name" -> "env") ~ ("prefix" -> "env")
      )
      val jPlots: JValue = List(
        ("q" -> "avg:system.load.1{*}by{env}") ~ ("display_type" -> "line") ~ ("style" ->
          ("palette" -> "dog_classic") ~ ("line_type" -> "solid") ~ ("line_width" -> "normal")) ~
          ("metadata" -> List(("expression" -> "avg:system.load.1{*}by{env}") ~ ("alias_name" -> "load")))
      )
      val jYaxis: JValue = ("scale" -> "linear") ~ ("include_zero" -> true)
      val jWidget: JValue = ("definition" -> ("requests" -> jPlots) ~ ("yaxis" -> jYaxis) ~
        ("title" -> "graph2") ~ ("show_legend" -> true) ~ ("type" -> "timeseries")) ~
        ("layout" -> ("x" -> 1) ~ ("y" -> 1) ~ ("width" -> 50) ~ ("height" -> 30))
      json mustBe ("title" -> "Sample") ~ ("layout_type" -> "free") ~ ("widgets" -> List(jWidget)) ~
        ("description" -> "Sample dashboard") ~ ("is_read_only" -> false) ~ ("notify_list" -> List("user@test.com")) ~
        ("template_variables" -> jVars) ~ ("template_variable_presets" -> jPresets)
    }
    "produce a valid payload for Change" in {
      import Visualization.Change._
      val w1 = Widget.Ordered(graph(
        plot("system.load.1").aggregate(MetricAggregator.Sum).filterBy("env" -> "staging")
          .groupBy("host").compareTo(TimeBase.DayBefore)
          .sortBy(ChangeOrder.Change, SortDirection.Descending).showPresent
      ).withTitle("graph2"))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
      val json = Extraction.decompose(request)
      json mustBe ("title" -> "Sample") ~ ("layout_type" -> "ordered") ~ ("widgets" -> List[JValue](
        ("definition" -> ("title" -> "graph2") ~ ("type" -> "change") ~ ("requests" -> List[JValue](
          ("compare_to" -> "day_before") ~ ("order_by" -> "change") ~ ("order_dir" -> "desc") ~
            ("increase_good" -> true) ~ ("change_type" -> "absolute") ~ ("show_present" -> true) ~
            ("q" -> "sum:system.load.1{env:staging}by{host}"))))
      )) ~ ("is_read_only" -> false) ~ ("notify_list" -> List.empty[JValue]) ~
        ("template_variables" -> List.empty[JValue]) ~ ("template_variable_presets" -> List.empty[JValue])
    }
  }

  "Dashboard" should {
    "deserialize from valid JSON" in {
      val json =
        """
          |{
          |  "notify_list":["john@test.com"],
          |  "description":"Sample dashboard",
          |  "author_name":"John Smith",
          |  "template_variable_presets":[{
          |    "template_variables":[{
          |      "name":"env",
          |      "value":"qa"
          |    }],
          |    "name":"qa"
          |  }],
          |  "template_variables":[{
          |    "default":"uralian.test.host",
          |    "prefix":"host",
          |    "name":"host"
          |  },{
          |    "prefix":"env",
          |    "name":"env"
          |  }],
          |  "is_read_only":false,
          |  "id":"vwh-zv9-vww",
          |  "title":"Sample",
          |  "url":"/dashboard/vwh-zv9-vww/sample",
          |  "created_at":"2020-05-08T16:52:25.973528+00:00",
          |  "modified_at":"2020-05-08T16:52:25.973528+00:00",
          |  "author_handle":"john.smith@test.com",
          |  "widgets":[{
          |    "definition":{
          |      "requests":[{
          |        "q":"avg:system.cpu.user{*}",
          |        "style":{
          |          "line_width":"normal",
          |          "palette":"dog_classic",
          |          "line_type":"solid"
          |        },
          |        "display_type":"bars",
          |        "metadata":[{
          |          "alias_name":"usr",
          |          "expression":"avg:system.cpu.user{*}"
          |        }]
          |      },{
          |        "q":"avg:system.cpu.system{*}",
          |        "style":{
          |          "line_width":"normal",
          |          "palette":"dog_classic",
          |          "line_type":"solid"
          |        },
          |        "display_type":"line",
          |        "metadata":[{
          |          "alias_name":"sys",
          |          "expression":"avg:system.cpu.system{*}"
          |        }]
          |      }],
          |      "title":"graph1",
          |      "type":"timeseries",
          |      "show_legend":false,
          |      "yaxis":{
          |        "include_zero":true,
          |        "scale":"log",
          |        "min":"1"
          |      }
          |    },
          |    "id":6319423624107516
          |  }],
          |  "layout_type":"ordered"
          |}
          |""".stripMargin
      val dashboard = Serialization.read[Dashboard](json)
      dashboard.id mustBe "vwh-zv9-vww"
      dashboard.title mustBe "Sample"
      dashboard.url mustBe "/dashboard/vwh-zv9-vww/sample"
      dashboard.description.value mustBe "Sample dashboard"
      dashboard.authorName mustBe "John Smith"
      dashboard.authorHandle mustBe "john.smith@test.com"
      dashboard.createdAt mustBe java.time.LocalDateTime.of(
        2020, 5, 8, 16, 52, 25, 973528000
      ).toInstant(ZoneOffset.UTC)
      dashboard.modifiedAt mustBe java.time.LocalDateTime.of(
        2020, 5, 8, 16, 52, 25, 973528000
      ).toInstant(ZoneOffset.UTC)
      dashboard.isReadOnly mustBe false
      dashboard.layoutType mustBe LayoutType.Ordered
      dashboard.notifyList mustBe List("john@test.com")
      dashboard.templateVars mustBe List(
        TemplateVar("host", Some("host"), Some("uralian.test.host")),
        TemplateVar("env", Some("env"))
      )
      dashboard.presets mustBe List(Preset("qa", Map("env" -> "qa")))
    }
  }
}
