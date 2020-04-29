package com.uralian.woof.api.graphs

import com.uralian.woof.api.MetricQuery
import enumeratum.{Enum, EnumEntry}

/**
 * Graph vizualization type.
 *
 * @param entryName entry name.
 */
sealed abstract class Visualization(override val entryName: String) extends EnumEntry

/**
 * Available visuzalization types.
 */
object Visualization extends Enum[Visualization] {

  case object Timeseries extends Visualization("timeseries") {

    def graph(plots: TimeseriesPlot*) = TimeseriesDefinition(plots)

    def plot(queries: (MetricQuery, Option[String])*) = TimeseriesPlot(queries)
  }

  case object QueryValue extends Visualization("query_value") {

    def graph(plot: QueryValuePlot) = QueryValueDefinition(plot)

    def plot(query: MetricQuery) = QueryValuePlot(query)
  }

  case object QueryTable extends Visualization("query_table") {

    def graph(columns: QueryTableColumn*) = QueryTableDefinition(columns)

    def column(metric: String) = QueryTableColumn(metric)
  }

  case object Heatmap extends Visualization("heatmap")

  case object Scatter extends Visualization("scatterplot")

  case object Distribution extends Visualization("distribution")

  case object Toplist extends Visualization("toplist")

  case object Change extends Visualization("change")

  case object Hostmap extends Visualization("hostmap")

  val values = findValues
}