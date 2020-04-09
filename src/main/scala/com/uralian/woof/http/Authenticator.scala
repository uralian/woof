package com.uralian.woof.http

import enumeratum.{Enum, EnumEntry}
import sttp.client.Identity

/**
 * Augments HTTP requests by injecting security information into them.
 */
sealed trait Authenticator extends EnumEntry {

  /**
   * Creates a request transformer for the given API key and Application key.
   *
   * @param apiKey         DataDog API Key.
   * @param applicationKey DataDog Application Key.
   * @tparam R response type.
   * @return a Request->Request transformer for the DataDog client.
   */
  def apply[R](apiKey: String, applicationKey: Option[String]): RequestTransformer[R]
}

/**
 * Available authenticators.
 */
object Authenticator extends Enum[Authenticator] {

  /**
   * Adds DataDog API key and Application key as request headers.
   */
  case object AddHeaders extends Authenticator {
    def apply[R](apiKey: String, applicationKey: Option[String]): RequestTransformer[R] = request => {
      val headers = (ApiKeyHeader -> apiKey) +: applicationKey.map(ApplicationKeyHeader -> _).toList
      request.headers(headers.toMap)
    }
  }

  /**
   * Adds DataDog API key as a query parameter.
   */
  case object AddQueryParam extends Authenticator {
    def apply[R](apiKey: String, applicationKey: Option[String]): RequestTransformer[R] = request => {
      val uri = request.uri.param(ApiKeyParam, apiKey)
      request.copy[Identity, DDResponse[R], Nothing](uri = uri)
    }
  }

  val values = findValues
}