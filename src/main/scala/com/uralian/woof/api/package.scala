package com.uralian.woof

import com.uralian.woof.util.JsonUtils
import org.json4s.DefaultFormats

/**
 * Helper methods and types for API.
 */
package object api {

  val apiFormats = DefaultFormats ++ JsonUtils.commonSerializers + Tag.serializer

  /**
   * Implicitly converts a tuple (String, String) into a DataDog Tag.
   *
   * @param pair name->value pair.
   * @return a new Tag.
   */
  implicit def pairToTag(pair: (String, String)) = Tag(pair._1, pair._2)
}
