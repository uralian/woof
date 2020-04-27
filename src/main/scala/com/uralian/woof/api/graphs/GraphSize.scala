package com.uralian.woof.api.graphs

import enumeratum.EnumEntry.Lowercase
import enumeratum._

/**
 * Graph size.
 */
sealed trait GraphSize extends EnumEntry with Lowercase

/**
 * Available graph sizes.
 */
object GraphSize extends Enum[GraphSize] {

  case object Small extends GraphSize

  case object Medium extends GraphSize

  case object Large extends GraphSize

  case object XLarge extends GraphSize

  val Default = Medium

  val values = findValues
}