package com.uralian.woof.api

import enumeratum._

/**
 * Sorting direction for search results.
 *
 * @param entryName short form.
 */
sealed abstract class SortDirection(override val entryName: String) extends EnumEntry

/**
 * Available sorting directions.
 */
object SortDirection extends Enum[SortDirection] {

  case object Ascending extends SortDirection("asc")

  case object Descending extends SortDirection("desc")

  val values = findValues
}