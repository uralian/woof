package com.uralian.woof.api.hosts

import java.time._

import com.uralian.woof.api.hosts.HostQuery._
import com.uralian.woof.api.{QueryParams, SortDirection, Tag}
import com.uralian.woof.util.JsonUtils._

/**
 * Host searching query.
 *
 * @param filter        query filter.
 * @param sortField     field to sort the results by.
 * @param sortDirection sort direction.
 * @param start         result to start from.
 * @param count         maximum number of hosts to return.
 * @param from          point in time from which to start searching.
 */
final case class HostQuery(filter: Option[String] = None,
                           sortField: SortField = SortField.Default,
                           sortDirection: SortDirection = SortDirection.Ascending,
                           start: Int = 0,
                           count: Int = DefaultCount,
                           from: Instant = Instant.now minus DefaultTimeSpan) extends QueryParams {

  def withFilter(filter: String) = copy(filter = Some(filter))

  def searchHost(name: String) = withFilter("host:" + name)

  def searchTag(tag: Tag) = withFilter("tag:" + tag.toString)

  def searchAlias(alias: String) = withFilter("alias:" + alias)

  def sortedBy(field: SortField, direction: SortDirection) = copy(sortField = field, sortDirection = direction)

  def withStart(start: Int) = copy(start = start)

  def withCount(count: Int) = copy(count = count)

  def from(time: Instant) = copy(from = time)

  def forTimeSpan(duration: Duration) = copy(from = Instant.now minus duration)
}

/**
 * Provides JSON serializer for [[HostQuery]] instances.
 */
object HostQuery {
  val DefaultCount = 100

  val serializer = translateFields[HostQuery]("sortField" -> "sort_field", "sortDirection" -> "sort_direction")
}