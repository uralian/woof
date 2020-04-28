package com.uralian.woof.api.graphs

import enumeratum.EnumEntry.Lowercase
import enumeratum._

/**
 * Graph stroke type.
 */
sealed trait Stroke extends EnumEntry with Lowercase

/**
 * Available graph strokes.
 */
object Stroke extends Enum[Stroke] {

  case object Normal extends Stroke

  case object Thin extends Stroke

  case object Thick extends Stroke

  val Default = Normal

  val values = findValues
}