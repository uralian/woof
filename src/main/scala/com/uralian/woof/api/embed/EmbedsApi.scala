package com.uralian.woof.api.embed

import com.uralian.woof.api.AbstractHttpApi
import com.uralian.woof.http.Authenticator.AddHeaders
import com.uralian.woof.http.DataDogClient

import scala.concurrent.{ExecutionContext, Future}

/**
 * DataDog Embeddable Graphs API.
 */
trait EmbedsApi {

  /**
   * Returns all embeddable graphs.
   *
   * @return list of embeddable graphs.
   */
  def getAll(): Future[Seq[Embed]]

  /**
   * Retrieves an embeddable graph.
   *
   * @param embedId embeddable graph id.
   * @return an embeddable graph.
   */
  def get(embedId: String): Future[Embed]

  /**
   * Creates a new embeddable graph.
   *
   * @param request create request.
   * @return the newly created graph.
   */
  def create(request: CreateEmbed): Future[Embed]

  /**
   * Enables the specified graph.
   *
   * @param embedId embeddable graph id.
   * @return `true` if the operation was successful.
   */
  def enable(embedId: String): Future[Boolean]

  /**
   * Revokes the specified graph.
   *
   * @param embedId embeddable graph id.
   * @return `true` if the operation was successful.
   */
  def revoke(embedId: String): Future[Boolean]
}

/**
 * HTTP-based implementation of [[EmbedsApi]].
 *
 * @param client DataDog client.
 * @param ec     execution context.
 */
class EmbedsHttpApi(client: DataDogClient)(implicit ec: ExecutionContext)
  extends AbstractHttpApi(client) with EmbedsApi {

  val path = "v1/graph/embed"

  def getAll(): Future[Seq[Embed]] = apiGetJ(path) map { json =>
    (json \ "embedded_graphs").extract[Seq[Embed]]
  }

  def create(request: CreateEmbed): Future[Embed] = apiPost[CreateEmbed, Embed](path, request, AddHeaders)

  def get(embedId: String): Future[Embed] = apiGet[Embed](s"$path/$embedId")

  def enable(embedId: String): Future[Boolean] = apiGetJ(s"$path/$embedId/enable").map { json =>
    json.findField(_._1 == "success").isDefined
  }

  def revoke(embedId: String): Future[Boolean] = apiGetJ(s"$path/$embedId/revoke").map { json =>
    json.findField(_._1 == "success").isDefined
  }
}