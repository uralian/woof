package com.uralian.woof.api.graphs

import enumeratum.EnumEntry.Lowercase
import enumeratum._

/**
 * Graph rendering scale.
 */
sealed trait GraphScale extends EnumEntry with Lowercase

/**
 * Available rendering scales.
 */
object GraphScale extends Enum[GraphScale] {

  case object Linear extends GraphScale

  case object Log extends GraphScale

  case object Sqrt extends GraphScale

  case object Pow extends GraphScale

  val Default = Linear

  val values = findValues
}