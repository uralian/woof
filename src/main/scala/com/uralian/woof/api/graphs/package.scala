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

  /**
   * Adds functionality to FormatColor.
   *
   * @param underlying format color.
   */
  implicit class RichFormatColor(val underlying: FormatColor) extends AnyVal {
    /**
     * Creates a standard format palette using current color for text and the argument for background.
     *
     * @param bgColor background color.
     * @return standard format palette.
     */
    def on(bgColor: FormatColor): FormatPalette.Standard = FormatPalette.Standard(underlying, bgColor)
  }

}
