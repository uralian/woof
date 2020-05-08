package com.uralian.woof.api.dashboards

import com.uralian.woof.api.AbstractHttpApi
import com.uralian.woof.http.Authenticator.AddHeaders
import com.uralian.woof.http.DataDogClient

import scala.concurrent.{ExecutionContext, Future}

/**
 * DataDog Dashboards API.
 */
trait DashboardsApi {

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
}

/**
 * Http-based implementation of [[DashboardsApi]].
 *
 * @param client DataDog client.
 * @param ec     execution context.
 */
class DashboardsHttpApi(client: DataDogClient)(implicit ec: ExecutionContext)
  extends AbstractHttpApi(client) with DashboardsApi {

  private val path = s"v1/dashboard"

  def create(request: CreateDashboard[_ <: LayoutType]): Future[Dashboard] =
    apiPost[CreateDashboard[_ <: LayoutType], Dashboard](path, request, AddHeaders)

  def delete(dashboardId: String): Future[Boolean] = apiDeleteJ(s"$path/$dashboardId").map { json =>
    json.findField(_._1 == "deleted_dashboard_id").isDefined
  }
}