package com.uralian.woof.api.events

import enumeratum.EnumEntry.Lowercase
import enumeratum._

/**
 * DataDog alert type for alert-type events.
 */
sealed trait AlertType extends EnumEntry with Lowercase

/**
 * Enumerates valid alert types.
 */
object AlertType extends Enum[AlertType] {

  case object Info extends AlertType

  case object Success extends AlertType

  case object Warning extends AlertType

  case object Error extends AlertType

  val values = findValues
}