package com.uralian.woof.api

import com.uralian.woof.AbstractUnitSpec
import com.uralian.woof.api.graphs._
import org.json4s.Extraction
import org.json4s.JsonDSL._
import org.json4s.native.Serialization

/**
 * GraphsApi test suite.
 */
class GraphsApiSpec extends AbstractUnitSpec {

  "CreateGraph" should {
    "produce valid payload" in {
      val request = CreateGraph("avg:system.cpu.user{*}")
        .withQueries("avg:system.cpu.idle{*}")
        .withTitle("Sample Graph")
        .withTimeframe(Timeframe.Hour4)
        .withSize(GraphSize.XLarge)
        .withLegend
      val json = Extraction.decompose(request)
      json mustBe
        ("graph_json" ->
          """{"viz":"timeseries","events":[],"requests":[
            |{"q":"avg:system.cpu.user{*}","type":"line","metadata":{"avg:system.cpu.user{*}":"alias?"}},
            |{"q":"avg:system.cpu.idle{*}","type":"line","metadata":{"avg:system.cpu.idle{*}":"alias?"}}]}"""
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
