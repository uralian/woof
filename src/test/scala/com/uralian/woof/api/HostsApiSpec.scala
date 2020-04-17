package com.uralian.woof.api

import java.time.{Duration, Instant}

import com.uralian.woof.AbstractUnitSpec
import com.uralian.woof.api.hosts._
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.Serialization

/**
 * HostsApi test suite.
 */
class HostsApiSpec extends AbstractUnitSpec {

  "HostQuery" should {
    "produce valid query params" in {
      val time = currentTime() minus Duration.ofHours(3)
      val query = HostQuery()
        .searchHost("abcd")
        .sortedBy(SortField.Status, SortDirection.Descending)
        .withStart(100)
        .withCount(300)
        .from(time)
      val params = query.toParams
      params.toSet mustBe Set("filter" -> "host:abcd", "sort_field" -> "status", "sort_direction" -> "desc",
        "start" -> 100, "count" -> 300, "from" -> time.getEpochSecond)
    }
  }

  "HostInfo" should {
    "deserialize from valid JSON" in {
      val json =
        """
          |{
          |  "last_reported_time": 1577786283,
          |  "name": "test.host",
          |  "is_muted": false,
          |  "mute_timeout": 1577886283,
          |  "apps": ["ntp", "agent"],
          |  "tags_by_source": {
          |    "Datadog": ["woof:test", "host:test.host"]
          |  },
          |  "up": true,
          |  "metrics": {"load": 0.5, "iowait": 3.2, "cpu": 99},
          |  "sources": ["agent"],
          |  "meta": {},
          |  "host_name": "test.host",
          |  "id": 1495952100,
          |  "aliases": ["test.host"]
          |}
          |""".stripMargin
      val info = Serialization.read[HostInfo](json)
      info.name mustBe "test.host"
      info.lastReportedTime mustBe Instant.ofEpochSecond(1577786283)
      info.isMuted mustBe false
      info.muteTimeout.value mustBe Instant.ofEpochSecond(1577886283)
      info.apps mustBe Seq("ntp", "agent")
      info.tagsBySource mustBe Map("Datadog" -> Seq[Tag]("woof" -> "test", "host" -> "test.host"))
      info.up mustBe true
      info.metrics mustBe Map("load" -> 0.5, "iowait" -> 3.2, "cpu" -> 99)
      info.sources mustBe Seq("agent")
      info.hostName mustBe "test.host"
      info.id mustBe 1495952100
      info.aliases mustBe Seq("test.host")
    }
    "render toString as JSON" in {
      val info = HostInfo("test", currentTime(), true, None, Seq("a", "b"),
        Map("abc" -> Seq[Tag]("a" -> "bc"), "def" -> Seq[Tag]("d" -> "dd", "e" -> "ee")),
        false, Map("x" -> 1, "y" -> 2), Seq("src"), "test2", 11111, Seq("test3"))
      Serialization.read[HostInfo](info.toString) mustBe info
    }
  }

  "MuteRequest" should {
    "produce valid payload" in {
      val time = currentTime() plus Duration.ofDays(2)
      val req = MuteRequest(Some("Host is going down"), Some(time), false)
      val json = Extraction.decompose(req)
      json mustBe ("message" -> "Host is going down") ~ ("end" -> JLong(time.getEpochSecond)) ~ ("override" -> false)
    }
  }

  "HostResponse" should {
    "deserialize from valid JSON" in {
      val json =
        """
          |{
          |  "action": "Muted",
          |  "hostname": "test.host",
          |  "message": "Muting this host for a test!"
          |}
          |""".stripMargin
      val response = Serialization.read[HostResponse](json)
      response mustBe HostResponse(action = "Muted", hostname = "test.host", message = Some("Muting this host for a test!"))
    }
  }
}
