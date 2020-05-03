package com.uralian.woof.api.graphs

import enumeratum._

/**
 * Baseline a metric is compared to.
 *
 * @param entryName
 */
sealed abstract class TimeBase(override val entryName: String) extends EnumEntry

/**
 * Available baselines.
 */
object TimeBase extends Enum[TimeBase] {

  case object HourBefore extends TimeBase("hour_before")

  case object DayBefore extends TimeBase("day_before")

  case object WeekBefore extends TimeBase("week_before")

  case object MonthBefore extends TimeBase("month_before")

  val Default = HourBefore

  val values = findValues
}