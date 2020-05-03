package com.uralian.woof.api.graphs

import enumeratum.EnumEntry.Lowercase
import enumeratum._

/**
 * Graph line type.
 */
sealed trait LineType extends EnumEntry with Lowercase

/**
 * Available line types.
 */
object LineType extends Enum[LineType] {

  case object Solid extends LineType

  case object Dashed extends LineType

  case object Dotted extends LineType

  val Default = Solid

  val values = findValues
}