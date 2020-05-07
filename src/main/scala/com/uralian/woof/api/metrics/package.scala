package com.uralian.woof.api

import enumeratum.Json4s

/**
 * Helper methods and types for Metrics API.
 */
package object metrics {

  implicit val metricFormats = apiFormats +
    Json4s.serializer(MetricType) +
    UnitInfo.serializer +
    Point.serializer +
    Timeseries.serializer +
    MetricScale.serializer +
    CreateSeries.serializer +
    MetricMetadata.serializer
}
