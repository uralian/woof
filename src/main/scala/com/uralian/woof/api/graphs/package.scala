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
    CreateGraph.serializer

  /**
   * Adds functionality to MetricQuery.
   *
   * @param underlying metric query.
   */
  implicit class RichMetricQuery(val underlying: MetricQuery) extends AnyVal {
    /**
     * Adds an alias to metric query.
     *
     * @param alias query alias.
     * @return a pair MetricQuery->Some(alias).
     */
    def as(alias: String): (MetricQuery, Option[String]) = underlying -> Some(alias)
  }

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

  /**
   * Implicitly converts a metric query into a metric query w/o alias construct.
   *
   * @param query metric query.
   * @return a pair MetricQuery->None.
   */
  implicit def queryToQueryAlias(query: MetricQuery): (MetricQuery, Option[String]) = query -> None
}
