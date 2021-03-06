package com.uralian.woof.api

import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}

import com.uralian.woof.AbstractUnitSpec
import com.uralian.woof.api.SortDirection.Descending
import com.uralian.woof.api.dashboards._
import com.uralian.woof.api.dsl._
import com.uralian.woof.api.graphs.ColorPalette.Cool
import com.uralian.woof.api.graphs.FormatColor._
import com.uralian.woof.api.graphs.FormatComparator.{GE, GT}
import com.uralian.woof.api.graphs.GraphScale.{Log, Sqrt}
import com.uralian.woof.api.graphs.HostmapPalette.YellowGreen
import com.uralian.woof.api.graphs.QueryValueAggregator.Last
import com.uralian.woof.api.graphs.RankAggregator.L2Norm
import com.uralian.woof.api.graphs.TextAlign.Left
import com.uralian.woof.api.graphs.{AxisOptions, ChangeOrder, ColorPalette, ConditionalFormat, DisplayType, GraphScale, QueryValueAggregator, TimeBase}
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.Serialization

/**
 * DashboardsApi test suite.
 */
class DashboardsApiSpec extends AbstractUnitSpec {

  import MetricQuery._

  private val defaultJson = ("title" -> "Sample") ~ ("layout_type" -> "ordered") ~ ("is_read_only" -> false) ~
    ("template_variables" -> List.empty[JValue]) ~ ("template_variable_presets" -> List.empty[JValue]) ~
    ("notify_list" -> List.empty[JValue])

  "CreateDashboard" should {
    "produce a valid payload for Timeseries for Ordered layout" in {
      import graphs.Visualization.Timeseries._
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
      import graphs.Visualization.Timeseries._
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
      import graphs.Visualization.Change._
      val w1 = Widget.Ordered(graph(
        plot("system.load.1").aggregate(MetricAggregator.Sum).filterBy("env" -> "staging")
          .groupBy("host").compareTo(TimeBase.DayBefore)
          .sortBy(ChangeOrder.Change, SortDirection.Descending).showPresent
      ).withTitle("graph2"))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
      val json = Extraction.decompose(request)
      checkJson(json, ("widgets" -> List[JValue](
        ("definition" -> ("title" -> "graph2") ~ ("type" -> "change") ~ ("requests" -> List[JValue](
          ("compare_to" -> "day_before") ~ ("order_by" -> "change") ~ ("order_dir" -> "desc") ~
            ("increase_good" -> true) ~ ("change_type" -> "absolute") ~ ("show_present" -> true) ~
            ("q" -> "sum:system.load.1{env:staging}by{host}"))))
      )))
    }
    "produce a valid payload for Distribution" in {
      import graphs.Visualization.Distribution._
      val w1 = Widget.Ordered(graph(plot(metric("system.cpu.user").aggregate(MetricAggregator.Avg)
        .filterBy("env" -> "qa").groupBy("host"),
        direct("avg:system.cpu.user{env:qa} by {host}/2")
      ).withPalette(ColorPalette.Cool)).withTitle("graph2"))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
      val json = Extraction.decompose(request)
      checkJson(json, ("widgets" -> List[JValue](
        ("definition" -> ("title" -> "graph2") ~ ("show_legend" -> false) ~ ("type" -> "distribution") ~
          ("requests" -> List[JValue](
            ("q" -> "avg:system.cpu.user{env:qa}by{host}, avg:system.cpu.user{env:qa} by {host}/2") ~
              ("style" -> ("palette" -> "cool"))
          )))
      )))
    }
    "produce a valid payload for Heatmap" in {
      import graphs.Visualization.Heatmap._
      val w1 = Widget.Ordered(graph(plot(
        direct("avg:system.cpu.user{*}by{env}"), direct("avg:system.cpu.idle{$var}by{env}")
      ).withPalette(Cool)).withYAxis(AxisOptions(scale = Sqrt, includeZero = false)))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
      val json = Extraction.decompose(request)
      checkJson(json, ("widgets" -> List[JValue](
        ("definition" -> ("yaxis" -> ("scale" -> "sqrt") ~ ("include_zero" -> false)) ~ ("show_legend" -> false) ~
          ("type" -> "heatmap") ~ ("requests" -> List[JValue](
          ("q" -> "avg:system.cpu.user{*}by{env}, avg:system.cpu.idle{$var}by{env}") ~ ("style" -> ("palette" -> "cool"))
        )))
      )))
    }
    "produce a valid payload for Hostmap" in {
      import graphs.Visualization.Hostmap._
      val w1 = Widget.Ordered(graph(
        "system.load.1" -> MetricAggregator.Max, "system.cpu.user" -> MetricAggregator.Avg)
        .withPalette(YellowGreen)
        .withMin(5)
        .filterBy("role:server")
        .groupBy("env")
        .hideNoMetricHosts
        .hideNoGroupHosts
        .flipped
      )
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
      val json = Extraction.decompose(request)
      checkJson(json, ("widgets" -> List[JValue](
        ("definition" -> ("scope" -> List[JValue]("role:server")) ~ ("group" -> List[JValue]("env")) ~
          ("style" -> ("palette" -> "yellow_to_green") ~ ("palette_flip" -> true) ~ ("fill_min" -> "5")) ~
          ("no_group_hosts" -> false) ~ ("no_metric_hosts" -> false) ~ ("node_type" -> "host") ~
          ("type" -> "hostmap") ~ ("requests" ->
          ("fill" -> ("q" -> "max:system.load.1{role:server}by{env}")) ~
            ("size" -> ("q" -> "avg:system.cpu.user{role:server}by{env}"))
          ))))
      )
    }
    "produce a valid payload for QueryValue" in {
      import graphs.Visualization.QueryValue._
      val w1 = Widget.Ordered(graph(plot(metric("system.cpu.user").groupBy("host"))
        .aggregate(Last)
        .withFormats(ConditionalFormat(GE, 5).withStandardColors(Green on Red))
      ).withCustomUnit("mmm").withPrecision(2).withAlign(Left))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
      val json = Extraction.decompose(request)
      checkJson(json, ("widgets" -> List[JValue](
        ("definition" -> ("autoscale" -> true) ~ ("custom_unit" -> "mmm") ~ ("precision" -> 2) ~
          ("text_align" -> "left") ~ ("type" -> "query_value") ~ ("requests" -> List[JValue](
          ("q" -> "avg:system.cpu.user{*}by{host}") ~ ("aggregator" -> "last") ~ ("conditional_formats" -> List[JValue](
            ("comparator" -> ">=") ~ ("value" -> JDecimal(5)) ~ ("palette" -> "green_on_red")
          ))
        )))
      )))
    }
    "produce a valid payload for Scatterplot" in {
      import graphs.Visualization.Scatter._
      val w1 = Widget.Ordered(graph(
        axis("system.cpu.user").aggregate(MetricAggregator.Min).withOptions(label = Some("xxx")),
        axis("system.cpu.idle").rollup(QueryValueAggregator.Max).withOptions(scale = Log, max = Some(100))
      ).pointBy("env").colorBy("client"))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
      val json = Extraction.decompose(request)
      checkJson(json, ("widgets" -> List[JValue](
        ("definition" ->
          ("xaxis" -> ("label" -> "xxx") ~ ("scale" -> "linear") ~ ("include_zero" -> true)) ~
            ("yaxis" -> ("scale" -> "log") ~ ("max" -> "100") ~ ("include_zero" -> true)) ~
            ("color_by_groups" -> List("client")) ~
            ("type" -> "scatterplot") ~ ("requests" ->
            ("x" -> ("q" -> "min:system.cpu.user{*}by{env,client}") ~ ("aggregator" -> "avg")) ~
              ("y" -> ("q" -> "avg:system.cpu.idle{*}by{env,client}") ~ ("aggregator" -> "max"))))
      )))
    }
    "produce a valid payload for QueryTable" in {
      import graphs.Visualization.QueryTable._
      val w1 = Widget.Ordered(graph(
        column("system.cpu.user"), column("system.mem.used"), column("system.load.1")
      ).filterBy("env" -> "qa").groupBy("host").withKeyColumn(1))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
      val json = Extraction.decompose(request)
      checkJson(json, ("widgets" -> List[JValue](
        ("definition" -> ("type" -> "query_table") ~ ("requests" -> List[JValue](
          ("q" -> "avg:system.cpu.user{env:qa}by{host}") ~ ("aggregator" -> "avg") ~
            ("conditional_formats" -> JArray(Nil)) ~ ("alias" -> JNothing),
          ("q" -> "avg:system.mem.used{env:qa}by{host}") ~ ("aggregator" -> "avg") ~
            ("conditional_formats" -> JArray(Nil)) ~ ("order" -> "desc") ~ ("limit" -> 10) ~ ("alias" -> JNothing),
          ("q" -> "avg:system.load.1{env:qa}by{host}") ~ ("aggregator" -> "avg") ~
            ("conditional_formats" -> JArray(Nil)) ~ ("alias" -> JNothing)
        )))
      )))
    }
    "produce a valid payload for Toplist" in {
      import graphs.Visualization.Toplist._
      val w1 = Widget.Ordered(graph(plot(direct("avg:system.cpu.user{env:qa}by{host}"))
        .withRows(Descending, 20).aggregate(L2Norm).withFormats(ConditionalFormat(GT, 123, Red on White))))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w1))
      val json = Extraction.decompose(request)
      checkJson(json, ("widgets" -> List[JValue](
        ("definition" -> ("type" -> "toplist") ~ ("requests" -> List[JValue](
          ("q" -> "top(avg:system.cpu.user{env:qa}by{host}, 20, 'l2norm', 'desc')") ~
            ("conditional_formats" -> List[JValue](
              ("comparator" -> ">") ~ ("value" -> JDecimal(123)) ~ ("palette" -> "red_on_white")
            ))
        )))
      )))
    }
    "produce a valid payload for Group" in {
      val w1 = Widget.Ordered(graphs.Visualization.Heatmap.graph(graphs.Visualization.Heatmap.plot(
        direct("avg:system.cpu.user{*}by{env}"), direct("avg:system.cpu.idle{$var}by{env}")
      ).withPalette(Cool)).withYAxis(AxisOptions(scale = Sqrt, includeZero = false)))
      val w2 = Widget.Ordered(graphs.Visualization.Distribution.graph(graphs.Visualization.Distribution.plot(
        metric("system.cpu.user").aggregate(MetricAggregator.Avg)
          .filterBy("env" -> "qa").groupBy("host"),
        direct("avg:system.cpu.user{env:qa} by {host}/2")
      ).withPalette(ColorPalette.Cool)).withTitle("graph2"))
      val w = Widget.Ordered(WidgetGroup(Seq(w1, w2), Some("my_group")))
      val request = CreateDashboard("Sample", LayoutType.Ordered, Seq(w))
      val json = Extraction.decompose(request)
      val jw1: JValue = ("definition" -> ("yaxis" -> ("scale" -> "sqrt") ~ ("include_zero" -> false)) ~
        ("show_legend" -> false) ~ ("type" -> "heatmap") ~ ("requests" -> List[JValue](
        ("q" -> "avg:system.cpu.user{*}by{env}, avg:system.cpu.idle{$var}by{env}") ~ ("style" -> ("palette" -> "cool"))
      )))
      val jw2: JValue = ("definition" -> ("title" -> "graph2") ~ ("show_legend" -> false) ~ ("type" -> "distribution") ~
        ("requests" -> List[JValue](
          ("q" -> "avg:system.cpu.user{env:qa}by{host}, avg:system.cpu.user{env:qa} by {host}/2") ~
            ("style" -> ("palette" -> "cool"))
        )))
      checkJson(json, "widgets" -> List[JValue](
        ("definition" -> ("title" -> "my_group") ~ ("type" -> "group") ~ ("layout_type" -> "ordered") ~
          ("widgets" -> List[JValue](jw1, jw2)))
      ))
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
      dashboard.authorName.value mustBe "John Smith"
      dashboard.authorHandle.value mustBe "john.smith@test.com"
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

  "DashboardList" should {
    "deserialize from valid JSON" in {
      val json =
        """
          |{
          |    "is_favorite": true,
          |    "name": "Benchmarks",
          |    "dashboard_count": 9,
          |    "author": {
          |        "handle": "john@doe.com",
          |        "name": "John Doe"
          |    },
          |    "created": "2020-08-12T21:11:24.000000+00:00",
          |    "type": "manual_dashboard_list",
          |    "dashboards": null,
          |    "modified": "2020-08-14T13:52:24.000000+00:00",
          |    "id": 120741
          |}
          |""".stripMargin
      val dl = Serialization.read[DashboardList](json)
      dl.id mustBe 120741
      dl.name mustBe "Benchmarks"
      dl.author mustBe Author(name = "John Doe", email = None, handle = Some("john@doe.com"))
      dl.createdAt mustBe LocalDateTime.of(2020, 8, 12, 21, 11, 24).toInstant(ZoneOffset.UTC)
      dl.modifiedAt mustBe LocalDateTime.of(2020, 8, 14, 13, 52, 24).toInstant(ZoneOffset.UTC)
      dl.isFavorite mustBe true
      dl.dashboardCount mustBe 9
      dl.listType mustBe "manual_dashboard_list"
    }
    "render toString as JSON" in {
      val dl = DashboardList(111, "Benchmarks", Author(name = "John", email = None, handle = Some("john@doe.com")),
        Instant.ofEpochSecond(1577886283), Instant.ofEpochSecond(1577886283),
        true, 5, "manual_dashboard_list")
      Serialization.read[DashboardList](dl.toString) mustBe dl
    }
  }

  private def checkJson(json: JValue, expected: JValue) = json mustBe expected.merge(defaultJson)
}
