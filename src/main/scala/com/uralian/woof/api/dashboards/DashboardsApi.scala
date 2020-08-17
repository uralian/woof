package com.uralian.woof.api.dashboards

import com.uralian.woof.api.AbstractHttpApi
import com.uralian.woof.http.Authenticator.AddHeaders
import com.uralian.woof.http.DataDogClient
import org.json4s.JValue
import org.json4s.JsonDSL._

import scala.concurrent.{ExecutionContext, Future}

/**
 * DataDog Dashboards API.
 */
trait DashboardsApi {

  /* dashboards */

  /**
   * Creates a new dashboard.
   *
   * @param request new dashboard data.
   * @return a new dashboard.
   */
  def create(request: CreateDashboard[_ <: LayoutType]): Future[Dashboard]

  /**
   * Deletes the specified dashboard.
   *
   * @param dashboardId id of the dashboard to delete.
   * @return `true` if the operation was successful, `false` otherwise.
   */
  def delete(dashboardId: String): Future[Boolean]

  /* lists */

  /**
   * Retrieves all dashboard lists.
   *
   * @return a collection of registered dashboard lists.
   */
  def getAllLists(): Future[Seq[DashboardList]]

  /**
   * Retrieves the specified dashboard list.
   *
   * @param listId list id.
   * @return a dashboard list.
   */
  def getList(listId: Int): Future[DashboardList]

  /**
   * Creates a new dashboard list.
   *
   * @param name list id.
   * @return the newly created dashboard list.
   */
  def createList(name: String): Future[DashboardList]

  /**
   * Updates a dashboard list.
   *
   * @param listId list id.
   * @param name   list name to set.
   * @return the updated dashboard list.
   */
  def updateList(listId: Int, name: String): Future[DashboardList]

  /**
   * Deletes a dashboard list.
   *
   * @param listId list id.
   * @return `true` if the list was successfully deleted.
   */
  def deleteList(listId: Int): Future[Boolean]
}

/**
 * Http-based implementation of [[DashboardsApi]].
 *
 * @param client DataDog client.
 * @param ec     execution context.
 */
class DashboardsHttpApi(client: DataDogClient)(implicit ec: ExecutionContext)
  extends AbstractHttpApi(client) with DashboardsApi {

  private object paths {
    val db = s"v1/dashboard"
    val dblist = s"v2/dashboard/lists/manual"

    def listDbs(listId: Int) = s"$dblist/$listId/dashboards"
  }

  /* dashboards */

  def create(request: CreateDashboard[_ <: LayoutType]): Future[Dashboard] =
    apiPost[CreateDashboard[_ <: LayoutType], Dashboard](paths.db, request, AddHeaders)

  def delete(dashboardId: String): Future[Boolean] = apiDeleteJ(s"${paths.db}/$dashboardId").map { json =>
    json.findField(_._1 == "deleted_dashboard_id").isDefined
  }

  /* lists */

  def getAllLists(): Future[Seq[DashboardList]] = apiGetJ(paths.dblist) map { json =>
    (json \ "dashboard_lists").extract[Seq[DashboardList]]
  }

  def getList(listId: Int): Future[DashboardList] = apiGet[DashboardList](s"${paths.dblist}/$listId")

  def createList(name: String): Future[DashboardList] =
    apiPost[JValue, DashboardList](paths.dblist, "name" -> name, AddHeaders)

  def updateList(listId: Int, name: String): Future[DashboardList] =
    apiPut[JValue, DashboardList](s"${paths.dblist}/$listId", "name" -> name)

  def deleteList(listId: Int): Future[Boolean] = apiDeleteJ(s"${paths.dblist}/$listId") map { json =>
    (json \ "deleted_dashboard_list_id").toOption.isDefined
  }
}