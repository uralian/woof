package com.uralian.woof.api

import enumeratum.Json4s

/**
 * Helper methods and types for Graphs API.
 */
package object graphs {

  val graphEnumSerializers = List(
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

  implicit val metricFormats = apiFormats ++ graphEnumSerializers +
    ConditionalFormat.serializer +
    TimeseriesPlot.serializer +
    TimeseriesDefinition.serializer +
    QueryValuePlot.serializer +
    QueryValueDefinition.serializer +
    QueryTablePlot.serializer +
    QueryTableDefinition.serializer +
    HeatmapPlot.serializer +
    HeatmapDefinition.serializer +
    ScatterPlot.serializer +
    ScatterDefinition.serializer +
    DistributionPlot.serializer +
    DistributionDefinition.serializer +
    ToplistPlot.serializer +
    ToplistDefinition.serializer +
    ChangePlot.serializer +
    ChangeDefinition.serializer +
    HostmapStyle.serializer +
    HostmapDefinition.serializer +
    Graph.serializer +
    CreateGraph.serializer +
    CreateSnapshot.serializer
}
