package com.uralian.woof.api.metrics

import com.uralian.woof.util.JsonUtils._
import org.json4s.native.Serialization

import scala.concurrent.duration.Duration

/**
 * Metric metadata.
 *
 * @param metricType     metric type.
 * @param shortName      short metric name.
 * @param description    metric description.
 * @param unit           metric unit.
 * @param perUnit        metric time unit.
 * @param statsdInterval StatsD interval.
 */
final case class MetricMetadata(metricType: MetricType,
                                shortName: String,
                                description: Option[String] = None,
                                unit: Option[String] = None,
                                perUnit: Option[String] = None,
                                statsdInterval: Option[Duration] = None) {

  def withDescription(description: String) = copy(description = Some(description))

  def withUnit(unit: String) = copy(unit = Some(unit))

  def withPerUnit(perUnit: String) = copy(perUnit = Some(perUnit))

  def withStatsDInterval(ival: Duration) = copy(statsdInterval = Some(ival))

  override def toString: String = Serialization.write(this)
}

/**
 * Provides JSON serializer for [[MetricMetadata]].
 */
object MetricMetadata {
  val serializer = translateFields[MetricMetadata]("metricType" -> "type",
    "shortName" -> "short_name", "perUnit" -> "per_unit", "statsdInterval" -> "statsd_interval")
}