package com.uralian.woof.api.graphs

import enumeratum.EnumEntry.Lowercase
import enumeratum._

/**
 * Change order type.
 */
sealed trait ChangeOrder extends EnumEntry with Lowercase

/**
 * Available change orders.
 */
object ChangeOrder extends Enum[ChangeOrder] {

  case object Change extends ChangeOrder

  case object Name extends ChangeOrder

  case object Present extends ChangeOrder

  case object Past extends ChangeOrder

  val Default = Change

  val values = findValues
}
