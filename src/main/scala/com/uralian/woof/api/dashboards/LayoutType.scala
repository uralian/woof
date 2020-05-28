package com.uralian.woof.api.dashboards

import enumeratum.EnumEntry.Lowercase
import enumeratum._

/**
 * Dashboard layout type.
 */
sealed trait LayoutType extends EnumEntry with Lowercase

/**
 * Available layout types.
 */
object LayoutType extends Enum[LayoutType] {

  final case object Free extends LayoutType

  final case object Ordered extends LayoutType

  val values = findValues
}

/**
 * Widget layout.
 *
 * @param x
 * @param y
 * @param width
 * @param height
 */
final case class Layout(x: Int, y: Int, width: Int, height: Int) {
  require(x >= 0 && y >= 0, "X and Y cannot be negative")
  require(width >= 0 && height >= 0, "Width and Height cannot be negative")
}