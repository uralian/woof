package com.uralian.woof.api

import java.time.Duration

import com.uralian.woof.AbstractITSpec
import com.uralian.woof.api.hosts.{HostQuery, HostResponse, HostsApi, HostsHttpApi}
import com.uralian.woof.http.DataDogClient

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Hosts API test suite.
 */
class HostsHttpApiSpec extends AbstractITSpec {

  implicit val serialization = org.json4s.native.Serialization

  val client = DataDogClient()
  val api: HostsApi = new HostsHttpApi(client)

  "HostsHttpApi" should {
    "search for active hosts" in {
      val query = HostQuery(Some(s"host:$host"))
      val hosts = api.search(query).futureValue
      hosts.size mustBe 1
      val h = hosts.headOption.value
      h.name mustBe host
      h.tagsBySource.get("Datadog").value must contain allOf(Tag("woof", "test"), Tag("host", host))
    }
    "retrieve the total number of active hosts" in {
      val totals = api.totals(currentTime() minus Duration.ofDays(10)).futureValue
      totals.totalActive must be > 0
      totals.totalUp must be > 0
    }
    "mute a host" in {
      val response = api.mute(host, Some("muting now"), Some(currentTime().plusSeconds(120))).futureValue
      response mustBe HostResponse("Muted", host, Some("muting now"))
    }
    "unmute a host" in {
      val response = api.unmute(host).futureValue
      response mustBe HostResponse("Unmuted", host, None)
    }
  }
}
