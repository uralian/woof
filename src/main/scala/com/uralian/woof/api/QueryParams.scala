package com.uralian.woof.api

import org.json4s.{Extraction, Formats}

/**
 * Should be implemented by request classes that are marshalled as HTTP query parameters.
 */
trait QueryParams {
  /**
   * Converts this instance into a sequence of (name, value) pairs to be included into HTTP query string.
   *
   * @param formats JSON formats.
   * @return a sequence of (name, value) pairs.
   */
  def toParams(implicit formats: Formats): Seq[(String, Any)] = Extraction.decompose(this).extract[Map[String, _]].toSeq
}

/**
 * Provides default (empty) [[QueryParams]] instance.
 */
object QueryParams {
  val Empty = new QueryParams {}
}