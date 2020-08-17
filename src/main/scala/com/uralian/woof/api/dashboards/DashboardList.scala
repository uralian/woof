package com.uralian.woof.api.dashboards

import java.time.Instant

import com.uralian.woof.util.JsonUtils
import org.json4s.native.Serialization

/**
 * Author information.
 *
 * @param name   author's name.
 * @param email  author's email.
 * @param handle author's handle.
 */
final case class Author(name: String, email: Option[String], handle: Option[String])

/**
 * DataDog Dashboard list.
 *
 * @param id             list id.
 * @param name           list name.
 * @param author         creator information.
 * @param createdAt      time when the list was created.
 * @param modifiedAt     time when the list was modified.
 * @param isFavorite     whether the list has been marked as favorite.
 * @param dashboardCount number of dashboards in the list.
 * @param listType       list type.
 */
final case class DashboardList(id: Int,
                               name: String,
                               author: Author,
                               createdAt: Instant,
                               modifiedAt: Instant,
                               isFavorite: Boolean,
                               dashboardCount: Int,
                               listType: String) {
  override def toString: String = Serialization.write(this)
}

/**
 * Factory for [[DashboardList]] instances.
 */
object DashboardList extends JsonUtils {

  val serializer = translateFields[DashboardList]("createdAt" -> "created", "modifiedAt" -> "modified",
    "dashboardCount" -> "dashboard_count", "isFavorite" -> "is_favorite", "listType" -> "type")
}