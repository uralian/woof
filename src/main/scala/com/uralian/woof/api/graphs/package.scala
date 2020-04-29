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
    FormatColor,
    FormatPalette,
    FormatComparator,
    QueryValueAggregator,
    TextAlign,
    Timeframe,
    GraphSize) map (Json4s.serializer(_))

  implicit val metricFormats = apiFormats ++ enumSerializers +
    ConditionalFormat.serializer +
    TimeseriesPlot.serializer +
    TimeseriesDefinition.serializer +
    QueryValuePlot.serializer +
    QueryValueDefinition.serializer +
    QueryTablePlot.serializer +
    QueryTableDefinition.serializer +
    Graph.serializer +
    CreateGraph.serializer
}
