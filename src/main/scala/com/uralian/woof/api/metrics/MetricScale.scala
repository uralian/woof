package com.uralian.woof.api.metrics

import java.time.Duration

import com.uralian.woof.api.metrics.MetricType._
import org.json4s.JsonDSL._
import org.json4s._

/**
 * A combination of metric type and interval (if applicable).
 *
 * @param metricType metric type.
 */
sealed abstract class MetricScale(val metricType: MetricType)

/**
 * Factory for [[MetricScale]] instances.
 */
object MetricScale {

  import Extraction._

  final case class CountScale(interval: Option[Duration]) extends MetricScale(Count)

  final case class RateScale(interval: Option[Duration]) extends MetricScale(Rate)

  final case object GaugeScale extends MetricScale(Gauge)

  // deserializer not needed at this point
  val serializer: CustomSerializer[MetricScale] = new CustomSerializer[MetricScale](_ => ( {
    case jv => ???
  }, {
    case CountScale(interval) => ("type" -> decompose(Count)) ~ ("interval" -> decompose(interval))
    case RateScale(interval)  => ("type" -> decompose(Rate)) ~ ("interval" -> decompose(interval))
    case GaugeScale           => ("type" -> decompose(Gauge))
  }))
}