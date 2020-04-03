package com.uralian.woof.http

import com.uralian.woof.api.DataDogApiError
import grizzled.slf4j.Logging
import org.json4s.Formats
import org.json4s.native.Serialization
import sttp.client._
import sttp.client.asynchttpclient.future.AsyncHttpClientFutureBackend
import sttp.client.json4s._
import sttp.model.MediaType

import scala.concurrent.{ExecutionContext, Future}

/**
 * DataDog HTTP client.
 *
 * @param apiKey         DataDog API Key (needed for Reads and Writes).
 * @param applicationKey DataDog Application Key (needed for Reads).
 * @param site           DataDog site.
 */
class DataDogClient(apiKey: String, applicationKey: Option[String], site: DataDogSite = DataDogSite.Default)
  extends Logging {

  require(Option(apiKey).filterNot(_.trim.isEmpty).isDefined)

  import DataDogClient._

  private implicit val backend = AsyncHttpClientFutureBackend()

  private implicit val serialization = Serialization

  /**
   * Sends a GET request to a given path. It supplies the apiKey/applicationKey headers.
   *
   * @param path    path to the resource.
   * @param params  query parameters.
   * @param formats JSON formats.
   * @tparam R response type.
   * @return either a response type or exception as a future.
   * @throws DataDogApiError in case of an error returned by the backend.
   */
  def httpGet[R: Manifest](path: String, params: (String, Any)*)
                          (implicit formats: Formats, ec: ExecutionContext): Future[R] = {
    val uri = uri"${api(path)}?$params"
    val headers = (ApiKeyHeader -> apiKey) +: applicationKey.map(ApplicationKeyHeader -> _).toList
    debug(s"Sending GET request: $uri")
    val response = basicRequest
      .get(uri)
      .headers(headers.toMap)
      .response(asJson[R])
      .send()
    response map translateResponse[R]
  }

  /**
   * Sends a POST request to a given path. It apiKey as a query parameter.
   *
   * @param path    path to the resource.
   * @param request payload to encode to JSON and send as HTTP body.
   * @param params  query parameters.
   * @param formats JSON formats.
   * @tparam Q request type.
   * @tparam R response type.
   * @return either a response type or exception as a future.
   * @throws DataDogApiError in case of an error returned by the backend.
   */
  def httpPost[Q <: AnyRef, R: Manifest](path: String, request: Q, params: (String, Any)*)
                                        (implicit formats: Formats, ec: ExecutionContext): Future[R] = {
    val uri = uri"${api(path)}?api_key=$apiKey"
    val response = basicRequest
      .post(uri)
      .contentType(MediaType.ApplicationJson)
      .body(request)
      .response(asJson[R])
      .send()
    response map translateResponse[R]
  }

  /**
   * Concatenates DD site path with "api" component and the supplied path.
   *
   * @param path
   * @return
   */
  private def api(path: String) = site.url + "/api/" + path

  /**
   * Strips down the response to the message body, converting response errors into exception.
   *
   * @param rsp
   * @tparam R
   * @return
   */
  private def translateResponse[R: Manifest](rsp: Response[Either[ResponseError[Exception], R]]): R = rsp match {
    case Response(Right(body), _, _, _, _)       => body
    case Response(Left(error), code, text, _, _) => throw new DataDogApiError(code.code, text, error)
  }
}

/**
 * Factory for [[DataDogClient]] instances.
 */
object DataDogClient {
  /* header names */
  val ApiKeyHeader = "DD-API-KEY"
  val ApplicationKeyHeader = "DD-APPLICATION-KEY"

  /* environment variables */
  val ApiKeyEnv = "DATADOG_API_KEY"
  val ApplicationKeyEnv = "DATADOG_APPLICATION_KEY"
  val DatadogHostEnv = "DATADOG_HOST"

  /**
   * Creates a new instance of DataDog client using environment variables
   * for DATADOG_API_KEY, DATADOG_APPLICATION_KEY and DATADOG_HOST.
   *
   * @return
   */
  // $COVERAGE-OFF$
  def apply(): DataDogClient = {
    val apiKey = sys.env(ApiKeyEnv)
    val applicationKey = sys.env.get(ApplicationKeyEnv)
    val site = sys.env.get(DatadogHostEnv).map(DataDogSite.withName).getOrElse(DataDogSite.Default)
    new DataDogClient(apiKey, applicationKey, site)
  }
  // $COVERAGE-ON$
}
