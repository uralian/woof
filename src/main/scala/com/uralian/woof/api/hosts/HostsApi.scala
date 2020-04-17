package com.uralian.woof.api.hosts

import java.time.Instant

import com.uralian.woof.api.AbstractHttpApi
import com.uralian.woof.http.Authenticator.AddHeaders
import com.uralian.woof.http.DataDogClient
import org.json4s.JValue
import org.json4s.JsonAST.JNothing

import scala.concurrent.{ExecutionContext, Future}

/**
 * DataDog Hosts API.
 */
trait HostsApi {

  /**
   * Search for hosts by name, tag, or alias.
   *
   * @param query search query.
   * @return a list of [[HostInfo]] instances.
   */
  def search(query: HostQuery): Future[Seq[HostInfo]]

  /**
   * Retrieves the total number of up and active hosts.
   *
   * @param from point in time from which to search.
   * @return the total number of up and active hosts.
   */
  def totals(from: Instant = Instant.now minus DefaultTimeSpan): Future[HostTotals]

  /**
   * Mutes the specified host.
   *
   * @param host         host to mute.
   * @param message      optional message.
   * @param until        until what time the host needs to be muted (None means forever).
   * @param overrideFlag muting override flag.
   * @return action response message.
   */
  def mute(host: String,
           message: Option[String] = None,
           until: Option[Instant] = None,
           overrideFlag: Boolean = false): Future[HostResponse]

  /**
   * Unmutes the specified host.
   *
   * @param host host to unmute.
   * @return action response message.
   */
  def unmute(host: String): Future[HostResponse]
}

/**
 * HTTP-based implementation of [[HostsApi]].
 *
 * @param client DataDog client.
 * @param ec     execution context.
 */
class HostsHttpApi(client: DataDogClient)(implicit ec: ExecutionContext)
  extends AbstractHttpApi(client) with HostsApi {

  val hostsPath = "v1/hosts"
  val hostPath = "v1/host"

  def search(query: HostQuery): Future[Seq[HostInfo]] = apiGetJ(hostsPath, query) map { json =>
    (json \ "host_list").extract[Seq[HostInfo]]
  }

  def totals(from: Instant = Instant.now minus DefaultTimeSpan): Future[HostTotals] =
    apiGet[HostTotals](hostsPath + "/totals", "from" -> from.getEpochSecond)

  def mute(host: String,
           message: Option[String] = None,
           until: Option[Instant] = None,
           overrideFlag: Boolean = false): Future[HostResponse] = {
    val request = MuteRequest(message, until, overrideFlag)
    apiPost[MuteRequest, HostResponse](s"$hostPath/$host/mute", request, AddHeaders)
  }

  def unmute(host: String): Future[HostResponse] =
    apiPost[JValue, HostResponse](s"$hostPath/$host/unmute", JNothing, AddHeaders)
}