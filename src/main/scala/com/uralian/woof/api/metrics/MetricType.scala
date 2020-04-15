package com.uralian.woof.api.metrics

import enumeratum.EnumEntry.Lowercase
import enumeratum._

/**
 * DataDog metric type.
 */
sealed trait MetricType extends EnumEntry with Lowercase

/**
 * Available metric types.
 */
object MetricType extends Enum[MetricType] {

  case object Gauge extends MetricType

  case object Rate extends MetricType

  case object Count extends MetricType

  val values = findValues
}