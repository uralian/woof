package com.uralian.woof.api.events

import java.time.Instant

import com.uralian.woof.api._
import com.uralian.woof.util.JsonUtils._
import org.json4s.native.Serialization

/**
 * A request to create a new event in DataDog.
 *
 * @param title          event title.
 * @param text           event text.
 * @param dateHappened   time of the event (if skipped, DataDog will use the current time).
 * @param priority       event priority.
 * @param host           host of the event.
 * @param tags           event tags.
 * @param alertType      alert type if applicable.
 * @param aggregationKey event aggregation key.
 * @param sourceTypeName event source type name.
 * @param relatedEventId related event Id.
 * @param deviceName     device name.
 */
final case class CreateEventData(title: String,
                                 text: Option[String] = None,
                                 dateHappened: Option[Instant] = None,
                                 priority: Option[Priority] = None,
                                 host: Option[String] = None,
                                 tags: List[Tag] = Nil,
                                 alertType: Option[AlertType] = None,
                                 aggregationKey: Option[String] = None,
                                 sourceTypeName: Option[String] = None,
                                 relatedEventId: Option[Long] = None,
                                 deviceName: Option[String] = None) {

  def withText(text: String) = copy(text = Some(text))

  def withDateHappened(date: Instant) = copy(dateHappened = Some(date))

  def withPriority(priority: Priority) = copy(priority = Some(priority))

  def withHost(host: String) = copy(host = Some(host))

  def withTags(moreTags: Tag*) = copy(tags = tags ++ moreTags)

  def withAlertType(alertType: AlertType) = copy(alertType = Some(alertType))

  def withAggregationKey(key: String) = copy(aggregationKey = Some(key))

  def withSourceTypeName(name: String) = copy(sourceTypeName = Some(name))

  def withRelatedEventId(id: Long) = copy(relatedEventId = Some(id))

  def withDeviceName(name: String) = copy(deviceName = Some(name))

  override def toString: String = Serialization.write(this)
}

/**
 * Factory for [[CreateEventData]] instances.
 */
object CreateEventData {
  val serializer = translateFields[CreateEventData]("dateHappened" -> "date_happened",
    "alertType" -> "alert_type",
    "aggregationKey" -> "aggregation_key",
    "sourceTypeName" -> "source_type_name",
    "relatedEventId" -> "related_event_id",
    "deviceName" -> "device_name")
}
