package com.uralian.woof.api

import org.json4s._

/**
 * A DataDog tag.
 *
 * @param name  tag name.
 * @param value tag value.
 */
final case class Tag private(name: String, value: String) {
  override val toString: String = name + ":" + value
}

/**
 * Tag instance factory.
 */
object Tag {

  /**
   * Creates a new Tag instance converting the name and value into lowercase.
   *
   * @param name  tag name.
   * @param value tag value.
   * @return a new Tag instance.
   */
  def apply(name: String, value: String): Tag = new Tag(name.toLowerCase, value.toLowerCase)

  /**
   * Creates a new Tag instance from "name:value" formatted string.
   *
   * @param str string in the format "name:value".
   * @return a new Tag.
   */
  def apply(str: String): Tag = {
    val Array(name, value) = str.split(':')
    Tag(name, value)
  }

  /**
   * Tag JSON serializer.
   */
  val serializer: CustomSerializer[Tag] = new CustomSerializer[Tag](_ => ( {
    case JString(str) => Tag(str)
  }, {
    case tag: Tag => JString(tag.toString)
  }))
}
