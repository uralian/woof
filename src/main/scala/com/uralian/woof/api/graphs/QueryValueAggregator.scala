package com.uralian.woof.api.graphs

import enumeratum.EnumEntry.Lowercase
import enumeratum._

/**
 * Aggregator for QueryValue plots.
 */
sealed trait QueryValueAggregator extends EnumEntry with Lowercase

/**
 * Available aggregators.
 */
object QueryValueAggregator extends Enum[QueryValueAggregator] {

  case object Avg extends QueryValueAggregator

  case object Max extends QueryValueAggregator

  case object Min extends QueryValueAggregator

  case object Sum extends QueryValueAggregator

  case object Last extends QueryValueAggregator

  val Default = Avg

  val values = findValues
}
