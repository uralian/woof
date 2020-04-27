package com.uralian.woof.api.graphs

import enumeratum.{Enum, EnumEntry}

/**
 * Live graph timeframe.
 *
 * @param entryName entry name.
 */
sealed abstract class Timeframe(override val entryName: String) extends EnumEntry

/**
 * Available graph timeframes.
 */
object Timeframe extends Enum[Timeframe] {

  case object Hour1 extends Timeframe("1_hour")

  case object Hour4 extends Timeframe("4_hours")

  case object Day1 extends Timeframe("1_day")

  case object Day2 extends Timeframe("2_days")

  case object Week1 extends Timeframe("1_week")

  val Default = Hour1

  val values = findValues
}
