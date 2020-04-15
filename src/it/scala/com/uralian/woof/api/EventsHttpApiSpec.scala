package com.uralian.woof.api

import com.uralian.woof.AbstractITSpec
import com.uralian.woof.api.events.{AlertType, CreateEvent, EventQuery, EventsApi, EventsHttpApi, Priority}
import com.uralian.woof.http.DataDogClient
import com.uralian.woof.util.Retry

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
 * Events API test suite.
 */
class EventsHttpApiSpec extends AbstractITSpec {

  implicit val serialization = org.json4s.native.Serialization

  val client = DataDogClient()
  val api: EventsApi = new EventsHttpApi(client)

  val ddTags = List[Tag]("testA" -> randomString(), "testB" -> randomString())

  var eventId: Long = _

  "EventsHttpApi" should {
    "create fully specified event in DataDog" in {
      val now = currentTime()
      val ced = CreateEvent("test")
        .withText("test event")
        .withDateHappened(now)
        .withPriority(Priority.Normal)
        .withHost("localhost")
        .withTags(ddTags: _*)
        .withAlertType(AlertType.Warning)
        .withAggregationKey("abcde")
        .withSourceTypeName("Jenkins")
        .withRelatedEventId(12345)
        .withDeviceName("sandbox")
      val event = api.create(ced).futureValue
      event.title mustBe "test"
      event.text.value mustBe "test event"
      event.dateHappened mustBe now
      event.priority.value mustBe Priority.Normal
      event.children mustBe empty
      event.relatedEventId.value mustBe 12345
      event.tags mustBe ddTags
      eventId = event.id
    }
    "create event with defaults in DataDog" in {
      val ced = CreateEvent("test")
      val event = api.create(ced).futureValue
      event.title mustBe "test"
      event.children mustBe empty
      event.tags mustBe empty
    }
    "retrieve an event from DataDog by ID" in {
      val event = Retry.retryFuture(10, 5 seconds)(() => api.get(eventId)).futureValue
      event.id mustBe eventId
      event.title mustBe "test"
      event.text.value mustBe "test event"
      event.alertType.value mustBe AlertType.Warning
      event.priority.value mustBe Priority.Normal
      event.host.value mustBe "localhost"
      event.children mustBe empty
      event.tags mustBe ddTags
    }
    "query event stream in DataDog by tags and time frame" in {
      Thread.sleep(5000)
      val from = currentTime().minusSeconds(3600 * 10)
      val to = currentTime().plusSeconds(3600 * 10)
      val query = EventQuery(from, to).withTags(ddTags: _*).noAggregation
      val events = Retry.retryFuture(5, 5 seconds)(() => api.query(query)).futureValue
      val event = events.head
      event.id mustBe eventId
      event.title mustBe "test"
      event.text.value mustBe "test event"
      event.alertType.value mustBe AlertType.Warning
      event.priority.value mustBe Priority.Normal
      event.host.value mustBe "localhost"
      event.children mustBe empty
      event.tags mustBe ddTags
    }
  }
}
