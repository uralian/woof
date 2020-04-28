package com.uralian.woof.api.graphs

import enumeratum.EnumEntry.Lowercase
import enumeratum._

/**
 * Text alignment.
 */
sealed trait TextAlign extends EnumEntry with Lowercase

/**
 * Available text alignments.
 */
object TextAlign extends Enum[TextAlign] {

  case object Left extends TextAlign

  case object Center extends TextAlign

  case object Right extends TextAlign

  val Default = Center

  val values = findValues
}