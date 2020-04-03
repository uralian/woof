package com.uralian.woof.api

import sttp.client.ResponseError

/**
 * Encapsulates a DataDog error sent by the backend as an HTTP response to the client.
 *
 * @param code  HTTP code.
 * @param text  HTTP status text.
 * @param cause the underlying response error.
 */
// $COVERAGE-OFF$
class DataDogApiError(val code: Int, val text: String, cause: ResponseError[_]) extends Exception(text, cause) {
  override def toString: String = s"DataDogBackendError(code=$code, text=$text, error=${cause.body})"
}
// $COVERAGE-ON$