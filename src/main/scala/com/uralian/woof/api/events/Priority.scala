package com.uralian.woof.api.events

import enumeratum.EnumEntry.Lowercase
import enumeratum._

/**
 * Event priority.
 */
sealed trait Priority extends EnumEntry with Lowercase

/**
 * Enumerates available priorities.
 */
object Priority extends Enum[Priority] {

  case object Normal extends Priority

  case object Low extends Priority

  val values = findValues
}
