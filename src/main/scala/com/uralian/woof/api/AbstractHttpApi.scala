package com.uralian.woof.api

import com.uralian.woof.http.{Authenticator, DataDogClient}
import org.json4s.JsonAST.JNothing
import org.json4s.{Formats, JValue}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Base class for HTTP-based APIs.
 *
 * @param client  DataDog client.
 * @param formats JSON formats.
 * @param ec      execution context.
 */
abstract class AbstractHttpApi(client: DataDogClient)(implicit formats: Formats, ec: ExecutionContext) {

  import Authenticator._

  /**
   * Executes HTTP POST and converts the result to the specified type.
   *
   * @param path    request path.
   * @param request value to be marshaled into JSON request body.
   * @param auth    security injector.
   * @tparam Q request type.
   * @tparam R response type.
   * @return future response as R.
   */
  protected def apiPost[Q <: AnyRef, R: Manifest](path: String, request: Q, auth: Authenticator = AddQueryParam): Future[R] =
    client.httpPost[Q, R](path, request, auth)

  /**
   * Executes HTTP POST and returns the result as JSON.
   *
   * @param path    request path.
   * @param request value to be marshaled into JSON request body.
   * @param auth    security injector.
   * @tparam Q request type.
   * @return future response as JSON.
   */
  protected def apiPostJ[Q <: AnyRef](path: String, request: Q, auth: Authenticator = AddQueryParam): Future[JValue] =
    apiPost[Q, JValue](path, request, auth)

  /**
   * Executes HTTP PUT and converts the result to the specified type.
   *
   * @param path    request path.
   * @param request value to be marshaled into JSON request body.
   * @param auth    security injector.
   * @tparam Q request type.
   * @tparam R response type.
   * @return future response as R.
   */
  protected def apiPut[Q <: AnyRef, R: Manifest](path: String, request: Q, auth: Authenticator = AddHeaders): Future[R] =
    client.httpPut[Q, R](path, request, auth)

  /**
   * Executes HTTP PUT and returns the result as JSON.
   *
   * @param path    request path.
   * @param request value to be marshaled into JSON request body.
   * @param auth    security injector.
   * @tparam Q request type.
   * @return future response as JSON.
   */
  protected def apiPutJ[Q <: AnyRef](path: String, request: Q, auth: Authenticator = AddHeaders): Future[JValue] =
    apiPut[Q, JValue](path, request, auth)

  /**
   * Executes HTTP DELETE and returns the result as JSON.
   *
   * @param path request path.
   * @param auth security injector.
   * @return future response as R.
   */
  protected def apiDeleteJ(path: String, auth: Authenticator = AddHeaders): Future[JValue] =
    apiDelete[JValue, JValue](path, JNothing, auth)

  /**
   * Executes HTTP DELETE and converts the result to the specified type.
   *
   * @param path    request path.
   * @param request value to be marshaled into JSON request body.
   * @param auth    security injector.
   * @tparam Q request type.
   * @tparam R response type.
   * @return future response as R.
   */
  protected def apiDelete[Q <: AnyRef, R: Manifest](path: String, request: Q, auth: Authenticator = AddHeaders): Future[R] =
    client.httpDelete[Q, R](path, request, auth)

  /**
   * Executes HTTP GET and converts the result to the specified type.
   *
   * @param path   request path.
   * @param params query parameters.
   * @tparam R response type.
   * @return future response as R.
   */
  protected def apiGet[R: Manifest](path: String, params: (String, Any)*): Future[R] =
    client.httpGet[R](path, AddHeaders, params: _*)

  /**
   * Executes HTTP GET and converts the result to the specified type.
   *
   * @param path  request path.
   * @param query query parameter object.
   * @tparam R response type.
   * @return future response as R.
   */
  protected def apiGet[R: Manifest](path: String, query: QueryParams): Future[R] = apiGet[R](path, query.toParams: _*)

  /**
   * Executes HTTP GET and returns the result as JSON.
   *
   * @param path   request path.
   * @param params query parameters.
   * @return future response as JSON.
   */
  protected def apiGetJ(path: String, params: (String, Any)*): Future[JValue] = apiGet[JValue](path, params: _*)

  /**
   * Executes HTTP GET and returns the result as JSON.
   *
   * @param path  request path.
   * @param query query parameter object.
   * @return future response as JSON.
   */
  protected def apiGetJ(path: String, query: QueryParams): Future[JValue] = apiGetJ(path, query.toParams: _*)
}