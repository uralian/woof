package com.uralian.woof.api.dashboards

import enumeratum._

/**
 * List item operation.
 */
sealed trait ItemOperation extends EnumEntry

/**
 * Available item operations.
 */
object ItemOperation extends Enum[ItemOperation] {

  final case object Add extends ItemOperation

  final case object Delete extends ItemOperation

  final case object Update extends ItemOperation

  val values = findValues
}