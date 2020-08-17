package com.uralian.woof.http

import java.net.URL

import com.uralian.woof.api.DataDogApiError
import grizzled.slf4j.Logging
import org.json4s.native.Serialization
import org.json4s.{Formats, MappingException}
import sttp.client._
import sttp.client.asynchttpclient.future.AsyncHttpClientFutureBackend
import sttp.client.json4s._
import sttp.model.MediaType.ApplicationJson
import sttp.model.Method

import scala.concurrent.{ExecutionContext, Future}

/**
 * DataDog HTTP client.
 *
 * @param apiKey         DataDog API Key (needed for Reads and Writes).
 * @param applicationKey DataDog Application Key (needed for Reads).
 * @param url            DataDog site URL.
 */
class DataDogClient(apiKey: String,
                    applicationKey: Option[String],
                    url: URL = new URL(DataDogSite.Default.url)) extends Logging {

  require(Option(apiKey).filterNot(_.trim.isEmpty).isDefined, "API Key undefined")

  info(s"DataDog client created: (apiKey=******, applicationKey=******, url=$url)")

  /**
   * Creates a new DataDog HTTP client.
   *
   * @param apiKey         DataDog API Key (needed for Reads and Writes).
   * @param applicationKey DataDog Application Key (needed for Reads).
   * @param site           DataDog site.
   */
  def this(apiKey: String, applicationKey: Option[String], site: DataDogSite) =
    this(apiKey, applicationKey, new URL(site.url))

  private implicit val backend = AsyncHttpClientFutureBackend()

  private implicit val serialization = Serialization

  /**
   * Sends a GET request to a given path. The security parameters are injected via the supplied [[SecurityInjector]].
   *
   * @param path     path to the resource.
   * @param security security injector.
   * @param params   query parameters.
   * @param formats  JSON formats.
   * @param ec       execution context.
   * @tparam R response type.
   * @return either a response type or exception as a future.
   * @throws DataDogApiError in case of an error returned by the backend.
   */
  def httpGet[R: Manifest](path: String, security: Authenticator, params: (String, Any)*)
                          (implicit formats: Formats, ec: ExecutionContext): Future[R] = {
    send[R](Method.GET, path, security(apiKey, applicationKey), r => r, params: _*)
  }

  /**
   * Sends a DELETE request to a given path. The security parameters are injected via the supplied [[SecurityInjector]].
   *
   * @param path     path to the resource.
   * @param payload  payload to encode to JSON and send as HTTP body.
   * @param security security injector.
   * @param params   query parameters.
   * @param formats  JSON formats.
   * @param ec       execution context.
   * @tparam Q request type.
   * @tparam R response type.
   * @return either a response type or exception as a future.
   * @throws DataDogApiError in case of an error returned by the backend.
   */
  def httpDelete[Q <: AnyRef, R: Manifest](path: String, payload: Q, security: Authenticator, params: (String, Any)*)
                                          (implicit formats: Formats, ec: ExecutionContext): Future[R] = {
    send[R](Method.DELETE, path, security(apiKey, applicationKey), _.contentType(ApplicationJson).body(payload), params: _*)
  }

  /**
   * Sends a POST request to a given path. The security parameters are injected via the supplied [[SecurityInjector]].
   *
   * @param path     path to the resource.
   * @param payload  payload to encode to JSON and send as HTTP body.
   * @param security security injector.
   * @param params   query parameters.
   * @param formats  JSON formats.
   * @param ec       execution context.
   * @tparam Q request type.
   * @tparam R response type.
   * @return either a response type or exception as a future.
   * @throws DataDogApiError in case of an error returned by the backend.
   */
  def httpPost[Q <: AnyRef, R: Manifest](path: String, payload: Q, security: Authenticator, params: (String, Any)*)
                                        (implicit formats: Formats, ec: ExecutionContext): Future[R] = {
    send[R](Method.POST, path, security(apiKey, applicationKey), _.contentType(ApplicationJson).body(payload), params: _*)
  }

  /**
   * Sends a PUT request to a given path. The security parameters are injected via the supplied [[SecurityInjector]].
   *
   * @param path     path to the resource.
   * @param payload  payload to encode to JSON and send as HTTP body.
   * @param security security injector.
   * @param params   query parameters.
   * @param formats  JSON formats.
   * @param ec       execution context.
   * @tparam Q request type.
   * @tparam R response type.
   * @return either a response type or exception as a future.
   * @throws DataDogApiError in case of an error returned by the backend.
   */
  def httpPut[Q <: AnyRef, R: Manifest](path: String, payload: Q, security: Authenticator, params: (String, Any)*)
                                       (implicit formats: Formats, ec: ExecutionContext): Future[R] = {
    send[R](Method.PUT, path, security(apiKey, applicationKey), _.contentType(ApplicationJson).body(payload), params: _*)
  }

  /**
   * Sends an HTTP request to a given path using the supplied method and path. The request will be augmented using
   * the supplied function and then secured with the provided [[SecurityInjector]].
   *
   * @param method       HTTP method to use.
   * @param path         request path.
   * @param authenticate security injector.
   * @param augment      function to augment the request with before sending.
   * @param params       query parameters.
   * @param formats      JSON formats.
   * @param ec           execution context.
   * @tparam R response type.
   * @return either a response type or exception as a future.
   * @throws DataDogApiError in case of an error returned by the backend.
   */
  private def send[R: Manifest](method: Method,
                                path: String,
                                authenticate: DDRequest[R] => DDRequest[R],
                                augment: DDRequest[R] => DDRequest[R],
                                params: (String, Any)*)
                               (implicit formats: Formats, ec: ExecutionContext): Future[R] = {
    val uri = uri"${apiPath(path)}?$params"
    debug(s"Sending $method request: $uri")
    val basic: DDRequest[R] = basicRequest
      .copy[Identity, Either[String, String], Nothing](uri = uri, method = method)
      .response(asJson[R])
    val request = (augment andThen authenticate) (basic)
    val response = request.send()
    response map translateResponse[R]
  }

  /**
   * Concatenates DD site path with "api" component and the supplied path.
   *
   * @param path
   * @return
   */
  private def apiPath(path: String) = url.toExternalForm + "/api/" + path

  /**
   * Strips down the response to the message body, converting response errors into exception.
   *
   * @param rsp
   * @tparam R
   * @return
   */
  private def translateResponse[R: Manifest](rsp: Response[Either[ResponseError[Exception], R]]): R = rsp match {
    case Response(Right(body), _, _, _, _)                                        => body
    case Response(Left(DeserializationError(_, e: MappingException)), _, _, _, _) => throw e
    case Response(Left(error), code, text, _, _)                                  => throw new DataDogApiError(code.code, text, error)
  }
}

/**
 * Factory for [[DataDogClient]] instances.
 */
object DataDogClient {

  /**
   * Creates a new instance of DataDog client using environment variables
   * for DATADOG_API_KEY, DATADOG_APPLICATION_KEY and DATADOG_HOST.
   *
   * @return
   */
  def apply(): DataDogClient = {
    val apiKey = sys.env(ApiKeyEnv)
    val applicationKey = sys.env.get(ApplicationKeyEnv)
    val url = new URL(sys.env.get(DatadogHostEnv).getOrElse(DataDogSite.Default.url))
    new DataDogClient(apiKey, applicationKey, url)
  }
}