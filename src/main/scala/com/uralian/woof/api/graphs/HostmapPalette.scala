package com.uralian.woof.api.graphs

import enumeratum._

/**
 * Hostmap color palette.
 *
 * @param entryName
 */
sealed abstract class HostmapPalette(override val entryName: String) extends EnumEntry

/**
 * Available color palettes.
 */
object HostmapPalette extends Enum[HostmapPalette] {

  case object GreenOrange extends HostmapPalette("green_to_orange")

  case object YellowGreen extends HostmapPalette("yellow_to_green")

  case object Warm extends HostmapPalette("YlOrRd")

  case object Cool extends HostmapPalette("hostmap_blues")

  case object Plasma extends HostmapPalette("Plasma")

  case object Viridis extends HostmapPalette("Viridis")

  val Default = GreenOrange

  val values = findValues
}