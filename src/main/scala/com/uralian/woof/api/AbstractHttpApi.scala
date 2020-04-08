package com.uralian.woof.api

import com.uralian.woof.http.DataDogClient
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

  protected val NoParams = QueryParams.Empty

  /**
   * Executes HTTP POST and returns the result as JSON.
   *
   * @param path    request path.
   * @param request value to be marshaled into JSON request body.
   * @tparam Q request type.
   * @return future response as JValue.
   */
  protected def post[Q <: AnyRef](path: String, request: Q): Future[JValue] = client.httpPost[Q, JValue](path, request)

  /**
   * Executes HTTP POST and returns the result as a value of provided type.
   *
   * @param path      request path.
   * @param request   value to be marshaled into JSON request body.
   * @param extractor converts result JSON into a value of provided type.
   * @tparam Q request type.
   * @tparam R response type.
   * @return future response as R.
   */
  protected def postAndExtract[Q <: AnyRef, R: Manifest](path: String, request: Q, extractor: JValue => R): Future[R] =
    post(path, request) map extractor

  /**
   * Executes HTTP GET and returns the result as JSON.
   *
   * @param path  request path.
   * @param query value to be marshaled as query parameters.
   * @return future response as JValue.
   */
  protected def get(path: String, query: QueryParams): Future[JValue] = client.httpGet[JValue](path, query.toParams: _*)

  /**
   * Executes HTTP GET and returns the result as a value of provided type.
   *
   * @param path      request path.
   * @param query     value to be marshaled as query parameters.
   * @param extractor converts result JSON into a value of provided type.
   * @tparam R result type.
   * @return future response as R.
   */
  protected def getAndExtract[R: Manifest](path: String, query: QueryParams, extractor: JValue => R): Future[R] =
    get(path, query) map extractor
}
