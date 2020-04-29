package com.uralian.woof.api

import com.uralian.woof.AbstractUnitSpec
import com.uralian.woof.api.MetricAggregator._
import com.uralian.woof.api.MetricQuery._
import com.uralian.woof.api.graphs.ColorPalette._
import com.uralian.woof.api.graphs.DisplayType._
import com.uralian.woof.api.graphs.GraphDSL._
import com.uralian.woof.api.graphs.LineType._
import com.uralian.woof.api.graphs.Stroke._
import com.uralian.woof.api.graphs._
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.Serialization

/**
 * GraphsApi test suite.
 */
class GraphsApiSpec extends AbstractUnitSpec {

  "TimeseriesPlot" should {
    import Visualization.Timeseries._
    "produce valid JSON for a single query without alias" in {
      val p = plot(metric("system.cpu.idle").filterBy("env" -> "qa")).withStyle(Warm, Dashed, Thin)
      val json = Extraction.decompose(p)
      json mustBe ("q" -> "avg:system.cpu.idle{env:qa}") ~ ("type" -> "line") ~ ("style" ->
        ("palette" -> "warm") ~ ("type" -> "dashed") ~ ("width" -> "thin")
        ) ~ ("metadata" -> JNothing)
    }
    "produce valid JSON for a single query with alias" in {
      val p = plot(metric("system.cpu.idle").aggregate(Min).as("a1")).displayAs(Bars)
      val json = Extraction.decompose(p)
      json mustBe ("q" -> "min:system.cpu.idle{*}") ~ ("type" -> "bars") ~ ("style" ->
        ("palette" -> "dog_classic") ~ ("type" -> "solid") ~ ("width" -> "normal")
        ) ~ ("metadata" -> ("min:system.cpu.idle{*}" -> ("alias" -> "a1")))
    }
    "produce valid JSON for a multiple queries" in {
      val p = plot(
        metric("system.cpu.user").aggregate(Min).as("a1"),
        metric("system.cpu.idle").aggregate(Avg).as("a2"),
        (metric("system.cpu.user").aggregate(Min) / metric("system.cpu.idle").aggregate(Avg)).as("a3")
      ).displayAs(Area)
      val json = Extraction.decompose(p)
      json mustBe ("q" -> "min:system.cpu.user{*}, avg:system.cpu.idle{*}, min:system.cpu.user{*}/avg:system.cpu.idle{*}") ~
        ("type" -> "area") ~ ("style" -> ("palette" -> "dog_classic") ~ ("type" -> "solid") ~ ("width" -> "normal")) ~
        ("metadata" -> ("min:system.cpu.user{*}" -> ("alias" -> "a1")) ~
          ("avg:system.cpu.idle{*}" -> ("alias" -> "a2")) ~
          ("min:system.cpu.user{*}/avg:system.cpu.idle{*}" -> ("alias" -> "a3")))
    }
  }

  "TimeseriesDefinition" should {
    import Visualization.Timeseries._
    "produce valid JSON" in {
      val g = graph(
        plot(metric("system.cpu.idle").groupBy("host")),
        plot(metric("system.cpu.user").filterBy("env" -> "qa")),
        plot(text("avg:system.cpu.user{*}by{host}"))
      ).withYAxis(scale = GraphScale.Log, max = Some(10), includeZero = false)
      val json = Extraction.decompose(g)
      val style = ("palette" -> "dog_classic") ~ ("type" -> "solid") ~ ("width" -> "normal")
      json mustBe ("requests" -> List(
        ("q" -> "avg:system.cpu.idle{*}by{host}") ~ ("type" -> "line") ~ ("style" -> style) ~ ("metadata" -> JNothing),
        ("q" -> "avg:system.cpu.user{env:qa}") ~ ("type" -> "line") ~ ("style" -> style) ~ ("metadata" -> JNothing),
        ("q" -> "avg:system.cpu.user{*}by{host}") ~ ("type" -> "line") ~ ("style" -> style) ~ ("metadata" -> JNothing)
      )) ~ ("yaxis" -> ("scale" -> "log") ~ ("max" -> JDecimal(10)) ~ ("includeZero" -> false)) ~ ("viz" -> "timeseries")
    }
  }

  "ConditionalFormat" should {
    import FormatColor._
    import FormatComparator._
    "produce valid JSON for standard colors" in {
      val fmt = ConditionalFormat(LT, 100, White on Green)
      val json = Extraction.decompose(fmt)
      json mustBe ("comparator" -> "<") ~ ("value" -> JDecimal(100)) ~ ("palette" -> "white_on_green") ~ ("hide_value" -> false)
    }
    "fail on invalid color combination" in {
      an[AssertionError] must be thrownBy ConditionalFormat(LT, 100, Red on Red)
    }
    "produce valid JSON for custom text color" in {
      val fmt = ConditionalFormat(GE, 5).withCustomTextColor(0x12dd10).withHiddenValue
      val json = Extraction.decompose(fmt)
      json mustBe ("comparator" -> ">=") ~ ("value" -> JDecimal(5)) ~ ("palette" -> "custom_text") ~
        ("custom_fg_color" -> "#12dd10") ~ ("hide_value" -> true)
    }
    "produce valid JSON for custom background color" in {
      val fmt = ConditionalFormat(GE, 1).withCustomBackgroundColor(0x12dd11)
      val json = Extraction.decompose(fmt)
      json mustBe ("comparator" -> ">=") ~ ("value" -> JDecimal(1)) ~ ("palette" -> "custom_bg") ~
        ("custom_bg_color" -> "#12dd11") ~ ("hide_value" -> false)
    }
    "produce valid JSON for custom image" in {
      val fmt = ConditionalFormat(GE, 1).withImageUrl("www.yahoo.com")
      val json = Extraction.decompose(fmt)
      json mustBe ("comparator" -> ">=") ~ ("value" -> JDecimal(1)) ~ ("palette" -> "custom_image") ~
        ("image_url" -> "www.yahoo.com") ~ ("hide_value" -> false)
    }
  }

  "QueryValuePlot" should {
    "produce a valid JSON" in {
      import FormatColor._
      import FormatComparator._
      import QueryValueAggregator._
      val p = QueryValuePlot(text("max:system.cpu.user")).aggregate(Last).withFormats(
        ConditionalFormat(LT, 3).withStandardColors(Green on White).withHiddenValue,
        ConditionalFormat(GT, 5).withCustomTextColor(0xFF0000)
      )
      val json = Extraction.decompose(p)
      json mustBe ("q" -> "max:system.cpu.user") ~ ("aggregator" -> "last") ~ ("conditional_formats" -> List(
        ("comparator" -> "<") ~ ("value" -> JDecimal(3)) ~ ("palette" -> "green_on_white") ~ ("hide_value" -> true),
        ("comparator" -> ">") ~ ("value" -> JDecimal(5)) ~ ("palette" -> "custom_text") ~
          ("custom_fg_color" -> "#ff0000") ~ ("hide_value" -> false)
      ))
    }
  }

  "QueryValueDefinition" should {
    import FormatColor._
    import FormatComparator._
    import QueryValueAggregator._
    import TextAlign._
    import Visualization.QueryValue._
    "produce a valid JSON" in {
      val g = graph(plot(metric("system.mem.free").groupBy("host")).aggregate(Last)
        .withFormats(ConditionalFormat(GE, 5).withStandardColors(Green on Red))
      ).withCustomUnit("mmm").withPrecision(2).withAlign(Left)
      val json = Extraction.decompose(g)
      json mustBe ("autoscale" -> true) ~ ("custom_unit" -> "mmm") ~ ("precision" -> 2) ~ ("text_align" -> "left") ~
        ("viz" -> "query_value") ~ ("requests" -> List(
        ("q" -> "avg:system.mem.free{*}by{host}") ~ ("aggregator" -> "last") ~ ("conditional_formats" -> List(
          ("comparator" -> ">=") ~ ("value" -> JDecimal(5)) ~ ("palette" -> "green_on_red") ~ ("hide_value" -> false)
        ))
      ))
    }
  }

  "QueryTablePlot" should {
    import FormatColor._
    import FormatComparator._
    import SortDirection._
    import Visualization.QueryTable._
    "produce a valid JSON" in {
      val c = column("system.mem.free")
        .wrapIn("hour_before(", ")")
        .aggregate(MetricAggregator.Min)
        .rollup(QueryValueAggregator.Last)
        .as("a1")
        .withFormats(ConditionalFormat(GE, 5).withStandardColors(Green on Red))
      val plot = c.toPlot(Some(DataWindow(Ascending, 20)), Scope.Filter("env" -> "dev"), Seq(TagName("host")))
      val json = Extraction.decompose(plot)
      json mustBe ("q" -> "hour_before(min:system.mem.free{env:dev}by{host})") ~ ("aggregator" -> "last") ~
        ("alias" -> "a1") ~ ("conditional_formats" -> List(
        ("comparator" -> ">=") ~ ("value" -> JDecimal(5)) ~ ("palette" -> "green_on_red") ~ ("hide_value" -> false)
      )) ~ ("order" -> "asc") ~ ("limit" -> 20)
    }
  }

  "QueryTableDefinition" should {
    import QueryValueAggregator._
    import SortDirection._
    import Visualization.QueryTable._
    "produce a valid JSON" in {
      val g = graph(
        column("system.mem.free").as("a1"),
        column("system.mem.total").rollup(Last).as("a2")
      ).withKeyColumn(1).withRows(Ascending, 5)
        .filterBy("env" -> "qa").groupBy("client", "host")
      val json = Extraction.decompose(g)
      json mustBe ("viz" -> "query_table") ~ ("requests" -> List(
        ("q" -> "avg:system.mem.free{env:qa}by{client,host}") ~ ("aggregator" -> "avg") ~
          ("alias" -> "a1") ~ ("conditional_formats" -> List.empty[JValue]),
        ("q" -> "avg:system.mem.total{env:qa}by{client,host}") ~ ("aggregator" -> "last") ~
          ("alias" -> "a2") ~ ("conditional_formats" -> List.empty[JValue]) ~ ("order" -> "asc") ~ ("limit" -> 5)
      ))
    }
  }

  "HeatmapPlot" should {
    import ColorPalette._
    import Visualization.Heatmap._
    "produce a valid JSON" in {
      val p = plot(
        text("avg:system.cpu.user{$cluster} by {env}"),
        metric("system.cpu.idle").wrapIn("timeshift", 600).filterBy("$var").groupBy("env")
      ).withPalette(Cool)
      val json = Extraction.decompose(p)
      json mustBe ("q" -> "avg:system.cpu.user{$cluster} by {env}, timeshift(avg:system.cpu.idle{$var}by{env},600)") ~
        ("style" -> ("palette" -> "cool") ~ ("type" -> "solid") ~ ("width" -> "normal"))
    }
  }

  "HeatmapDefinition" should {
    import ColorPalette._
    import GraphScale._
    import Visualization.Heatmap._
    "produce a valid JSON" in {
      val g = graph(plot(text("avg:system.cpu.user{*}by{env}"), text("avg:system.cpu.idle{$var}by{env}")
      ).withPalette(Cool)).withYAxis(scale = Sqrt, includeZero = false)
      val json = Extraction.decompose(g)
      json mustBe ("yaxis" -> ("scale" -> "sqrt") ~ ("includeZero" -> false)) ~ ("viz" -> "heatmap") ~
        ("requests" -> List(
          ("q" -> "avg:system.cpu.user{*}by{env}, avg:system.cpu.idle{$var}by{env}") ~
            ("style" -> ("palette" -> "cool") ~ ("type" -> "solid") ~ ("width" -> "normal"))
        ))
    }
  }

  "CreateGraph" should {
    "produce valid payload" in {
      val request = CreateGraph(Visualization.Timeseries.graph(
        Visualization.Timeseries.plot(text("avg:system.cpu.user{*}")),
        Visualization.Timeseries.plot(text("avg:system.cpu.idle{*}"))))
        .withTitle("Sample Graph")
        .withTimeframe(Timeframe.Hour4)
        .withSize(GraphSize.XLarge)
        .withLegend
      val json = Extraction.decompose(request)
      json mustBe
        ("graph_json" ->
          """{"requests":[
            |{"q":"avg:system.cpu.user{*}","type":"line","style":{"palette":"dog_classic","type":"solid","width":"normal"}},
            |{"q":"avg:system.cpu.idle{*}","type":"line","style":{"palette":"dog_classic","type":"solid","width":"normal"}}],
            |"yaxis":{"scale":"linear","includeZero":true},"viz":"timeseries"}"""
            .stripMargin.replaceAll("\n", "")) ~
          ("timeframe" -> "4_hours") ~ ("size" -> "xlarge") ~ ("title" -> "Sample Graph") ~ ("legend" -> "yes")
    }
  }

  "Graph" should {
    "deserialize from valid JSON" in {
      val json =
        """
          |{
          |  "embed_id": "1446a4539dbff2e0bf136fc69e4c2f831f70da7e2730b74c21244eed402bd5c7",
          |  "template_variables": [
          |    "var"
          |  ],
          |  "html": "<iframe src=\"https://app.datadoghq.com?token=44eed402bd5c7&var=*\" width=\"800\"></iframe>"
          |  "graph_title": "Average CPU Load",
          |  "revoked": false,
          |  "dash_url": null,
          |  "shared_by": 1286061,
          |  "dash_name": null
          |}
          |""".stripMargin
      val embed = Serialization.read[Graph](json)
      embed.id mustBe "1446a4539dbff2e0bf136fc69e4c2f831f70da7e2730b74c21244eed402bd5c7"
      embed.templateVariables mustBe Seq("var")
      embed.html mustBe "<iframe src=\"https://app.datadoghq.com?token=44eed402bd5c7&var=*\" width=\"800\"></iframe>"
      embed.title mustBe "Average CPU Load"
      embed.revoked mustBe false
    }
    "render toString as JSON" in {
      val embed = Graph("12345", Seq("a", "b"), "<iframe/>", "graph", true, Some("dash1"), None, Some(1234))
      Serialization.read[Graph](embed.toString) mustBe embed
    }
  }
}
