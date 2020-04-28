package com.uralian.woof.api

import enumeratum.Json4s

/**
 * Helper methods and types for Graphs API.
 */
package object graphs {

  val enumSerializers = List(
    Visualization,
    DisplayType,
    ColorPalette,
    LineType,
    Stroke,
    Timeframe,
    GraphSize) map (Json4s.serializer(_))

  implicit val metricFormats = apiFormats ++ enumSerializers +
    TimeseriesPlot.serializer +
    GraphDefinition.serializer +
    Graph.serializer +
    CreateGraph.serializer
}
