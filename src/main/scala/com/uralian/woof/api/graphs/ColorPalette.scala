package com.uralian.woof.api.graphs

import enumeratum._

/**
 * Graph color palettes.
 *
 * @param entryName
 */
sealed abstract class ColorPalette(override val entryName: String) extends EnumEntry

/**
 * Available color palettes.
 */
object ColorPalette extends Enum[ColorPalette] {

  case object Classic extends ColorPalette("dog_classic")

  case object Cool extends ColorPalette("cool")

  case object Warm extends ColorPalette("warm")

  case object Purple extends ColorPalette("purple")

  case object Orange extends ColorPalette("orange")

  case object Gray extends ColorPalette("grey")

  val Default = Classic

  val values = findValues
}