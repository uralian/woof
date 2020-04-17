package com.uralian.woof.api

import java.time.Duration

import enumeratum.Json4s

/**
 * Helper methods and types for Hosts API.
 */
package object hosts {

  val DefaultTimeSpan = Duration.ofHours(2)

  implicit val metricFormats = apiFormats +
    Json4s.serializer(SortField) +
    Json4s.serializer(SortDirection) +
    HostQuery.serializer +
    HostInfo.serializer +
    HostTotals.serializer
}
