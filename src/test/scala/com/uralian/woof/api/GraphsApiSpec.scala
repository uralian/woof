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
    "produce valid JSON" in {
      val graph = timeseries(
        plot(metric("system.cpu.idle").groupBy("host")),
        plot(metric("system.cpu.user").filterBy("env" -> "qa")),
        plot(text("avg:system.cpu.user{*}by{host}"))
      ).withYAxis(scale = GraphScale.Log, max = Some(10), includeZero = false)
      val json = Extraction.decompose(graph)
      val style = ("palette" -> "dog_classic") ~ ("type" -> "solid") ~ ("width" -> "normal")
      json mustBe ("requests" -> List(
        ("q" -> "avg:system.cpu.idle{*}by{host}") ~ ("type" -> "line") ~ ("style" -> style) ~ ("metadata" -> JNothing),
        ("q" -> "avg:system.cpu.user{env:qa}") ~ ("type" -> "line") ~ ("style" -> style) ~ ("metadata" -> JNothing),
        ("q" -> "avg:system.cpu.user{*}by{host}") ~ ("type" -> "line") ~ ("style" -> style) ~ ("metadata" -> JNothing)
      )) ~ ("yaxis" -> ("scale" -> "log") ~ ("max" -> JDecimal(10)) ~ ("includeZero" -> false)) ~ ("viz" -> "timeseries")
    }
  }

  "CreateGraph" should {
    "produce valid payload" in {
      val request = CreateGraph(timeseries(
        plot(text("avg:system.cpu.user{*}")),
        plot(text("avg:system.cpu.idle{*}"))))
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
