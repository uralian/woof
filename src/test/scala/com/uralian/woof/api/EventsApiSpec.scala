package com.uralian.woof.api

import java.time.Instant

import com.uralian.woof.AbstractUnitSpec
import com.uralian.woof.api.events._
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.Serialization

/**
 * EventsApi test suite.
 */
class EventsApiSpec extends AbstractUnitSpec {

  "CreateEvent" should {
    val now = currentTime()
    val request = CreateEvent("test")
      .withText("test event")
      .withDateHappened(now)
      .withPriority(Priority.Normal)
      .withHost("localhost")
      .withTags("a" -> "AA", "b" -> "BB")
      .withAlertType(AlertType.Warning)
      .withAggregationKey("abcde")
      .withSourceTypeName("Jenkins")
      .withRelatedEventId(12345)
      .withDeviceName("sandbox")
    "produce valid payload" in {
      val json = Extraction.decompose(request)
      json mustBe ("title" -> "test") ~ ("text" -> "test event") ~ ("date_happened" -> JLong(now.getEpochSecond)) ~
        ("priority" -> "normal") ~ ("host" -> "localhost") ~ ("tags" -> List("a:aa", "b:bb")) ~
        ("alert_type" -> "warning") ~ ("aggregation_key" -> "abcde") ~ ("source_type_name" -> "Jenkins") ~
        ("related_event_id" -> 12345) ~ ("device_name" -> "sandbox")
    }
    "render toString as JSON" in {
      Serialization.read[CreateEvent](request.toString) mustBe request
    }
  }

  "ChildEvent" should {
    "deserialize from valid JSON" in {
      val json = """{"id": "123454321", "alert_type": "warning", "date_happened": 1586289000}"""
      val event = Serialization.read[ChildEvent](json)
      event.id mustBe "123454321"
      event.dateHappened mustBe Instant.ofEpochSecond(1586289000)
      event.alertType.value mustBe AlertType.Warning
    }
    "render toString as JSON" in {
      val event = ChildEvent("11111111", currentTime(), Some(AlertType.Success))
      Serialization.read[ChildEvent](event.toString) mustBe event
    }
  }

  "Event" should {
    "deserialize from valid JSON" in {
      val json =
        """{
          |  "date_happened": 1586288727,
          |  "alert_type": "info",
          |  "is_aggregate": false,
          |  "title": "Test event",
          |  "url": "/event/event?id=5402697430310445758",
          |  "text": "Woof test event",
          |  "tags": [
          |    "env:qa",
          |    "client:woof"
          |  ],
          |  "comments": [],
          |  "children": [
          |    {"id": "123454321", "alert_type": "warning", "date_happened": 1586289000}
          |  ]
          |  "device_name": null,
          |  "priority": "normal",
          |  "source": "My Apps",
          |  "host": null,
          |  "resource": "/api/v1/events/5402697430310445758",
          |  "id": 5402697430310445758
          |}
          |""".stripMargin

      val event = Serialization.read[Event](json)
      event.id mustBe 5402697430310445758L
      event.title mustBe "Test event"
      event.text.value mustBe "Woof test event"
      event.dateHappened mustBe Instant.ofEpochSecond(1586288727)
      event.handle mustBe empty
      event.alertType.value mustBe AlertType.Info
      event.priority.value mustBe Priority.Normal
      event.source.value mustBe "My Apps"
      event.host mustBe empty
      event.resource.value mustBe "/api/v1/events/5402697430310445758"
      event.isAggregate.value mustBe false
      event.relatedEventId mustBe empty
      event.deviceName mustBe empty
      event.tags mustBe List[Tag]("env" -> "qa", "client" -> "woof")
      event.url mustBe "/event/event?id=5402697430310445758"
      event.children mustBe Seq(
        ChildEvent("123454321", Instant.ofEpochSecond(1586289000), Some(AlertType.Warning))
      )
    }
    "render toString as JSON" in {
      val event = Event(12345, "event", None, Instant.ofEpochSecond(33333333), None, Some(AlertType.Info),
        None, None, None, None, Some(true), Nil, None, Some("device"), List("a" -> "b"), "/event/123")
      Serialization.read[Event](event.toString) mustBe event
    }
  }

  "EventQuery" should {
    "produce valid query params" in {
      val to = currentTime()
      val from = to.minusSeconds(3600 * 2)
      val query = EventQuery(from, to)
        .withPriority(Priority.Low)
        .withTags("A" -> "AAA", "B" -> "bbb")
        .withSources("src1", "src2")
        .noAggregation
      val params = query.toParams
      params.toSet mustBe Set(
        "start" -> from.getEpochSecond,
        "end" -> to.getEpochSecond,
        "priority" -> "low",
        "sources" -> "src1,src2",
        "tags" -> "a:aaa,b:bbb",
        "unaggregated" -> true
      )
    }
  }
}