package com.uralian.woof

import com.uralian.woof.api.MetricQuery.FreeformQuery
import com.uralian.woof.util.JsonUtils
import org.json4s.DefaultFormats

/**
 * Helper methods and types for API.
 */
package object api {

  /**
   * JSON formats.
   */
  val apiFormats = DefaultFormats.withBigDecimal ++ JsonUtils.commonSerializers +
    ScopeElement.serializer + Tag.serializer + TagName.serializer + VarName.serializer + Scope.serializer

  /**
   * Implicitly converts a tuple (String, String) into a DataDog Tag.
   *
   * @param pair name->value pair.
   * @return a new Tag.
   */
  implicit def pairToTag(pair: (String, String)): Tag = Tag(pair._1, pair._2)

  /**
   * Implicitly converts a string into a ScopeElement:
   * {{{
   * "key:value" -> Tag(key, value)
   * "name" -> TagName(name)
   * "$name" -> VarName(name)
   * }}}
   *
   * @param str
   * @return a new scope element.
   */
  implicit def stringToScopeElement(str: String): ScopeElement = ScopeElement(str)

  /**
   * Implicitly converts a string into a freeform query.
   *
   * @param str query text.
   * @return a new freeform metric query.
   */
  implicit def stringToMetricQuery(str: String): FreeformQuery = MetricQuery.text(str)
}
