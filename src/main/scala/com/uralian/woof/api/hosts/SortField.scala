package com.uralian.woof.api.hosts

import enumeratum.EnumEntry.Lowercase
import enumeratum._

/**
 * Field by which the search results need to be sorted.
 */
sealed trait SortField extends EnumEntry with Lowercase

/**
 * Available sorting fields.
 */
object SortField extends Enum[SortField] {

  case object Status extends SortField

  case object Apps extends SortField

  case object CPU extends SortField

  case object IOWait extends SortField

  case object Load extends SortField

  val Default = CPU

  val values = findValues
}
