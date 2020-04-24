package com.uralian.woof.api

import com.uralian.woof.api.Tag._
import org.json4s._

/**
 * A DataDog tag.
 *
 * @param name  tag name.
 * @param value tag value.
 */
final case class Tag private(name: String, value: String) {
  require(isValidName(name), "invalid tag name")
  require(isValidValue(value), "invalid tag value")
  require(name.size + value.size <= 200, "tag size too long")

  override val toString: String = name + ":" + value
}

/**
 * Tag instance factory.
 */
object Tag {

  private val nameRegex = """[a-z0-9_\-\./]+""".r

  private val valueRegex = """[a-z0-9:_\-\./]+""".r

  /**
   * Tests if the string is a valid tag name.
   *
   * @param str string to check.
   * @return `true` if the argument is a valid tag name.
   */
  def isValidName(str: String) = nameRegex.pattern.matcher(str).matches

  /**
   * Tests if the string is a valid tag value.
   *
   * @param str string to check.
   * @return `true` if the argument is a valid tag value.
   */
  def isValidValue(str: String) = valueRegex.pattern.matcher(str).matches

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
  def apply(str: String): Tag = str.split(':').toList match {
    case head :: tail => Tag(head, tail.mkString(":"))
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
