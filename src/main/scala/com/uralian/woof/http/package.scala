package com.uralian.woof

import sttp.client.{Request, ResponseError}

/**
 * Types and helper functions for DataDog HTTP client.
 */
package object http {
  
  /* http request/response types */
  type DDResponse[R] = Either[ResponseError[Exception], R]
  type DDRequest[R] = Request[DDResponse[R], Nothing]
  type RequestTransformer[R] = DDRequest[R] => DDRequest[R]

  /* header names */
  val ApiKeyHeader = "DD-API-KEY"
  val ApplicationKeyHeader = "DD-APPLICATION-KEY"

  /* query param names */
  val ApiKeyParam = "api_key"

  /* environment variables */
  val ApiKeyEnv = "DATADOG_API_KEY"
  val ApplicationKeyEnv = "DATADOG_APPLICATION_KEY"
  val DatadogHostEnv = "DATADOG_HOST"
}
