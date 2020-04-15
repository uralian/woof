package com.uralian.woof.api.metrics

import com.uralian.woof.api._
import org.json4s._

/**
 * Timeseries scope - either a list of tags or "*" wildcard for "all".
 */
sealed trait Scope

/**
 * Available scopes.
 */
object Scope {

  val AllToken = "*"

  final case class TagSet(tags: Seq[Tag]) extends Scope

  final case object All extends Scope

  /**
   * Creates a new Scope parsing the supplied string.
   *
   * @param str either "*" or a tag list.
   * @return a new Scope instance.
   */
  def apply(str: String): Scope = str match {
    case AllToken => All
    case _        => TagSet(decodeTags(str))
  }

  /**
   * Decomposes a scope into a string.
   *
   * @param scope metric scope.
   * @return either a list of tags encoded as String, or "*".
   */
  def unapply(scope: Scope): Option[String] = scope match {
    case All          => Some(AllToken)
    case TagSet(tags) => Some(encodeTags(tags))
  }

  /**
   * JSON serializer for scope instances.
   */
  val serializer: CustomSerializer[Scope] = new CustomSerializer[Scope](_ => ( {
    case JString(str) => apply(str)
  }, {
    case scope: Scope => unapply(scope).map(JString.apply).getOrElse(JNothing)
  }))
}