package com.uralian.woof

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
   * @return
   */
  implicit def stringToScopeElement(str: String): ScopeElement = ScopeElement(str)
}
