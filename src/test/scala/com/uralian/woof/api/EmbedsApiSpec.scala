package com.uralian.woof.api

import com.uralian.woof.AbstractUnitSpec
import com.uralian.woof.api.embed._
import org.json4s.Extraction
import org.json4s.JsonDSL._
import org.json4s.native.Serialization

/**
 * EmbedsApi test suite.
 */
class EmbedsApiSpec extends AbstractUnitSpec {

  "CreateEmbed" should {
    "produce valid payload" in {
      val request = CreateEmbed("avg:system.cpu.user{*}")
        .withQueries("avg:system.cpu.idle{*}")
        .withTitle("Sample Graph")
        .withTimeframe(Timeframe.Hour4)
        .withSize(GraphSize.XLarge)
        .withLegend
      val json = Extraction.decompose(request)
      json mustBe
        ("graph_json" -> """{"viz":"timeseries","events":[],"requests":[{"q":"avg:system.cpu.user{*}"},{"q":"avg:system.cpu.idle{*}"}]}""") ~
          ("timeframe" -> "4_hours") ~ ("size" -> "xlarge") ~ ("title" -> "Sample Graph") ~ ("legend" -> "yes")
    }
  }

  "Embed" should {
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
      val embed = Serialization.read[Embed](json)
      embed.id mustBe "1446a4539dbff2e0bf136fc69e4c2f831f70da7e2730b74c21244eed402bd5c7"
      embed.templateVariables mustBe Seq("var")
      embed.html mustBe "<iframe src=\"https://app.datadoghq.com?token=44eed402bd5c7&var=*\" width=\"800\"></iframe>"
      embed.title mustBe "Average CPU Load"
      embed.revoked mustBe false
    }
    "render toString as JSON" in {
      val embed = Embed("12345", Seq("a", "b"), "<iframe/>", "graph", true, Some("dash1"), None, Some(1234))
      Serialization.read[Embed](embed.toString) mustBe embed
    }
  }
}
