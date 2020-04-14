package com.uralian.woof

import com.uralian.woof.util.JsonUtils
import org.json4s.DefaultFormats

/**
 * Helper methods and types for API.
 */
package object api {

  val apiFormats = DefaultFormats.withBigDecimal ++ JsonUtils.commonSerializers + Tag.serializer

  /**
   * Implicitly converts a tuple (String, String) into a DataDog Tag.
   *
   * @param pair name->value pair.
   * @return a new Tag.
   */
  implicit def pairToTag(pair: (String, String)): Tag = Tag(pair._1, pair._2)

  /**
   * Encodes a list of tags as a comma separated string.
   *
   * @param tags tags to encode.
   * @return string in the format "key1:value1,key2:value2,..."
   */
  def encodeTags(tags: Seq[Tag]): String = tags.map(_.toString).mkString(",")

  /**
   * Decodes a list of tags from a string.
   *
   * @param str string in the format "key1:value1,key2:value2,..."
   * @return a list of tags.
   */
  def decodeTags(str: String): Seq[Tag] = str.split(",").map(Tag.apply).toSeq
}
