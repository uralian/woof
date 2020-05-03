package com.uralian.woof.api.graphs

import enumeratum.EnumEntry.Lowercase
import enumeratum._

/**
 * Aggregator for Toplist plots.
 */
sealed trait RankAggregator extends EnumEntry with Lowercase

/**
 * Available aggregators.
 */
object RankAggregator extends Enum[RankAggregator] {

  case object Mean extends RankAggregator

  case object Max extends RankAggregator

  case object Min extends RankAggregator

  case object Area extends RankAggregator

  case object L2Norm extends RankAggregator

  case object Last extends RankAggregator

  case object Sum extends RankAggregator

  val Default = Mean

  val values = findValues
}