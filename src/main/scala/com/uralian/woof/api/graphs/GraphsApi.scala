package com.uralian.woof.api.graphs

import com.uralian.woof.api.AbstractHttpApi
import com.uralian.woof.http.Authenticator.AddHeaders
import com.uralian.woof.http.DataDogClient

import scala.concurrent.{ExecutionContext, Future}

/**
 * Graphs API, combines DataDog Graph and Embeddable Graph APIs.
 */
trait GraphsApi {

  /**
   * Returns all live graphs.
   *
   * @return list of live graphs.
   */
  def getAll(): Future[Seq[Graph]]

  /**
   * Retrieves a live graph.
   *
   * @param graphId graph id.
   * @return a live graph.
   */
  def get(graphId: String): Future[Graph]

  /**
   * Creates a new live graph.
   *
   * @param request create request.
   * @return the newly created graph.
   */
  def create(request: CreateGraph): Future[Graph]

  /**
   * Enables the specified graph.
   *
   * @param graphId embeddable graph id.
   * @return `true` if the operation was successful.
   */
  def enable(graphId: String): Future[Boolean]

  /**
   * Revokes the specified graph.
   *
   * @param graphId graph id.
   * @return `true` if the operation was successful.
   */
  def revoke(graphId: String): Future[Boolean]
}

/**
 * HTTP-based implementation of [[GraphsApi]].
 *
 * @param client DataDog client.
 * @param ec     execution context.
 */
class GraphsHttpApi(client: DataDogClient)(implicit ec: ExecutionContext)
  extends AbstractHttpApi(client) with GraphsApi {

  private object paths {
    val embed = s"v1/graph/embed"
  }

  def getAll(): Future[Seq[Graph]] = apiGetJ(paths.embed) map { json =>
    (json \ "embedded_graphs").extract[Seq[Graph]]
  }

  def create(request: CreateGraph): Future[Graph] = apiPost[CreateGraph, Graph](paths.embed, request, AddHeaders)

  def get(graphId: String): Future[Graph] = apiGet[Graph](s"${paths.embed}/$graphId")

  def enable(graphId: String): Future[Boolean] = apiGetJ(s"${paths.embed}/$graphId/enable").map { json =>
    json.findField(_._1 == "success").isDefined
  }

  def revoke(graphId: String): Future[Boolean] = apiGetJ(s"${paths.embed}/$graphId/revoke").map { json =>
    json.findField(_._1 == "success").isDefined
  }
}