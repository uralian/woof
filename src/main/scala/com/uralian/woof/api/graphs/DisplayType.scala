package com.uralian.woof.api.graphs

import enumeratum.EnumEntry.Lowercase
import enumeratum._

/**
 * Graph display type.
 */
sealed trait DisplayType extends EnumEntry with Lowercase

/**
 * Available display types.
 */
object DisplayType extends Enum[DisplayType] {

  case object Line extends DisplayType

  case object Bars extends DisplayType

  case object Area extends DisplayType

  val values = findValues
}
