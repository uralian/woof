package com.uralian.woof.api

import enumeratum.Json4s

/**
 * Helper methods and types for Graphs API.
 */
package object graphs {

  implicit val metricFormats = apiFormats +
    Json4s.serializer(Timeframe) +
    Json4s.serializer(GraphSize) +
    Graph.serializer +
    CreateGraph.serializer
}
