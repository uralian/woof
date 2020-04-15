package com.uralian.woof.api

import java.time.Instant

import enumeratum.Json4s

/**
 * Helper methods and types for Metrics API.
 */
package object metrics {

  /**
   * Implicitly converts a tuple (Instant, Double) into a data Point.
   *
   * @param pair a tuple (time, value).
   * @return a new data point.
   */
  implicit def pairToPoint(pair: (Instant, Double)): Point = Point(pair._1, BigDecimal(pair._2))

  implicit val metricFormats = apiFormats +
    Json4s.serializer(MetricType) +
    UnitInfo.serializer +
    Scope.serializer +
    Point.serializer +
    Timeseries.serializer +
    MetricScale.serializer +
    CreateSeries.serializer +
    MetricMetadata.serializer
}
