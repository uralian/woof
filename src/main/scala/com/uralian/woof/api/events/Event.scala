package com.uralian.woof.api.events

import java.time.Instant

import com.uralian.woof.api._
import com.uralian.woof.util.JsonUtils._
import org.json4s.native.Serialization

/**
 * A DataDog event.
 *
 * @param id             event id.
 * @param title          event title.
 * @param text           event text.
 * @param dateHappened   time of the event.
 * @param handle         event handle.
 * @param alertType      alert type if applicable.
 * @param priority       event priority.
 * @param source         event source.
 * @param host           host of the event.
 * @param resource       event path for REST calls.
 * @param isAggregate    whether event is aggregate.
 * @param children       child events.
 * @param relatedEventId parent event id.
 * @param deviceName     event device name.
 * @param tags           event tags.
 * @param url            event URL.
 */
final case class Event(id: Long,
                       title: String,
                       text: Option[String],
                       dateHappened: Instant,
                       handle: Option[String],
                       alertType: Option[AlertType],
                       priority: Option[Priority],
                       source: Option[String],
                       host: Option[String],
                       resource: Option[String],
                       isAggregate: Option[Boolean],
                       children: Seq[ChildEvent],
                       relatedEventId: Option[Int],
                       deviceName: Option[String],
                       tags: List[ScopeElement],
                       url: String) {

  override def toString: String = Serialization.write(this)
}

/**
 * Provides event serializer.
 */
object Event {
  val serializer = translateFields[Event]("dateHappened" -> "date_happened",
    "alertType" -> "alert_type",
    "isAggregate" -> "is_aggregate",
    "relatedEventId" -> "related_event_id",
    "deviceName" -> "device_name")
}

/**
 * A DataDog child event.
 *
 * @param id           event id.
 * @param dateHappened time of the event.
 * @param alertType    alert type, if applicable.
 */
final case class ChildEvent(id: String, dateHappened: Instant, alertType: Option[AlertType]) {
  override def toString: String = Serialization.write(this)
}

/**
 * Provides child event serializer.
 */
object ChildEvent {
  val serializer = translateFields[ChildEvent]("dateHappened" -> "date_happened", "alertType" -> "alert_type")
}
