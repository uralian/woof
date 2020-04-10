package com.uralian.woof.api.tags

import com.uralian.woof.api.{AbstractHttpApi, Tag}
import com.uralian.woof.http.Authenticator.AddHeaders
import com.uralian.woof.http.DataDogClient
import org.json4s._

import scala.concurrent.{ExecutionContext, Future}

/**
 * DataDog Tags API.
 */
trait TagsApi {
  /**
   * Retrieves all tags with the hosts associated with them.
   *
   * @return a map of tags to the hosts associated with them.
   */
  def getAll(): Future[Map[Tag, Seq[String]]]

  /**
   * Retrieves all tags associated with a given host.
   *
   * @param host host to look up.
   * @return a full list of tags for the host.
   */
  def get(host: String): Future[Seq[Tag]]

  /**
   * Adds custom tags to the host.
   *
   * @param host   host.
   * @param source source information.
   * @param tags   tags to add.
   * @return a list of custom tags associated with that host.
   */
  def add(host: String, source: Option[String], tags: Tag*): Future[Seq[Tag]]

  /**
   * Replaces host's custom tags with a new set.
   *
   * @param host   host.
   * @param source source information.
   * @param tags   tags to replace the existing ones.
   * @return a list of custom tags associated with that host.
   */
  def update(host: String, source: Option[String], tags: Tag*): Future[Seq[Tag]]

  /**
   * Removes all custom tags from the host.
   *
   * @param host host.
   * @return nothing.
   */
  def remove(host: String): Future[Unit]
}

/**
 * HTTP-based implementation of [[TagsApi]].
 *
 * @param client DataDog client.
 * @param ec     execution context.
 */
class TagsHttpApi(client: DataDogClient)(implicit ec: ExecutionContext)
  extends AbstractHttpApi(client) with TagsApi {

  val path = "v1/tags/hosts"

  def getAll(): Future[Map[Tag, Seq[String]]] = apiGetJ(path) map { json =>
    val tags = json \ "tags"
    tags.extract[Map[String, List[String]]].map {
      case (str, hosts) => Tag(str) -> hosts
    }
  }

  def get(host: String): Future[Seq[Tag]] = apiGet[TagsResponse](s"$path/$host") map (_.tags)

  def add(host: String, source: Option[String], tags: Tag*): Future[Seq[Tag]] = {
    val request = TagsUpsertRequest(tags, source)
    apiPost[TagsUpsertRequest, TagsResponse](s"$path/$host", request, AddHeaders) map (_.tags)
  }

  def update(host: String, source: Option[String], tags: Tag*): Future[Seq[Tag]] = {
    val request = TagsUpsertRequest(tags, source)
    apiPut[TagsUpsertRequest, TagsResponse](s"$path/$host", request) map (_.tags)
  }

  def remove(host: String): Future[Unit] = apiDeleteJ(s"$path/$host") map (_ => {})
}
