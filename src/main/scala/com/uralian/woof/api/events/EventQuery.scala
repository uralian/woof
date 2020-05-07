package com.uralian.woof.api.events

import java.time.Instant

import com.uralian.woof.api._
import com.uralian.woof.util.JsonUtils
import org.json4s.FieldSerializer

/**
 * Event lookup request.
 *
 * @param start        start time.
 * @param end          end time.
 * @param priority     event priority.
 * @param sources      event sources.
 * @param tags         event tags.
 * @param unaggregated event aggregation flag.
 */
final case class EventQuery(start: Instant, end: Instant,
                            priority: Option[Priority] = None,
                            sources: Seq[String] = Nil,
                            tags: Seq[Tag] = Nil,
                            unaggregated: Option[Boolean] = None) extends QueryParams {

  def withPriority(priority: Priority) = copy(priority = Some(priority))

  def withSources(sources: String*) = copy(sources = sources)

  def withTags(moreTags: Tag*) = copy(tags = tags ++ moreTags)

  def noAggregation = copy(unaggregated = Some(true))
}

/**
 * Factory for [[EventQuery]] instances.
 */
object EventQuery extends JsonUtils {

  private val ser: FSer = {
    case ("tags", tags: Seq[_])       => Some("tags" -> tags.mkString(",")).filterNot(_._2.isEmpty)
    case ("sources", sources: Seq[_]) => Some("sources" -> sources.mkString(",")).filterNot(_._2.isEmpty)
  }

  val serializer = FieldSerializer[EventQuery](serializer = ser)
}
