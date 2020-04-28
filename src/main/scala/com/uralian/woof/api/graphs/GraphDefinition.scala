package com.uralian.woof.api.graphs

import com.uralian.woof.api.MetricQuery
import com.uralian.woof.api.graphs.Visualization._
import com.uralian.woof.util.JsonUtils._
import org.json4s.JsonDSL._
import org.json4s._

/**
 * DataDog graph definition.
 *
 * @tparam V visualization type.
 */
sealed trait GraphDefinition[V <: Visualization] {

  /**
   * Graph visualization type (Timeseries, Table, Heatmap etc.)
   *
   * @return graph visualization type.
   */
  def visualization: V

  /**
   * A list of plot definitions compatible with this visualization type.
   *
   * @return a list of plot definitions.
   */
  def plots: Seq[GraphPlot[V]]
}

/**
 * Provides JSON serializer for [[GraphDefinition]] instances.
 */
object GraphDefinition {
  val serializer = translateFields[GraphDefinition[_]]("visualization" -> "viz", "plots" -> "requests")
}

/**
 * A single plot inside a graph definition.
 *
 * @tparam V graph visualization type.
 */
sealed trait GraphPlot[V <: Visualization]

/**
 * Timeseries Plot.
 *
 * @param queries  a list of queries with optional aliases.
 * @param display  graph display type.
 * @param palette  color palette.
 * @param lineType graph line style.
 * @param stroke   graph line stroke.
 */
final case class TimeseriesPlot(queries: Seq[(MetricQuery, Option[String])],
                                display: DisplayType = DisplayType.Line,
                                palette: ColorPalette = ColorPalette.Default,
                                lineType: LineType = LineType.Default,
                                stroke: Stroke = Stroke.Default)
  extends GraphPlot[Timeseries.type] {

  def displayAs(display: DisplayType) = copy(display = display)

  def withPalette(palette: ColorPalette) = copy(palette = palette)

  def withLineType(lineType: LineType) = copy(lineType = lineType)

  def withStroke(stroke: Stroke) = copy(stroke = stroke)

  def withStyle(palette: ColorPalette, lineType: LineType, stroke: Stroke) =
    copy(palette = palette, lineType = lineType, stroke = stroke)
}

/**
 * Factory for [[TimeseriesPlot]] instances.
 */
object TimeseriesPlot {

  import Extraction._

  val serializer: CustomSerializer[TimeseriesPlot] = new CustomSerializer[TimeseriesPlot](_ => ( {
    case _ => ??? //todo implement for Dashboard API responses
  }, {
    case plot: TimeseriesPlot => {
      val style = ("palette" -> decompose(plot.palette)) ~ ("type" -> decompose(plot.lineType)) ~
        ("width" -> decompose(plot.stroke))
      val aliases = plot.queries.collect {
        case (query, Some(alias)) => query.q -> (("alias" -> alias): JValue)
      }
      val metadata = if (aliases.isEmpty) JNothing else JObject(aliases: _*)
      val queries = plot.queries.map(_._1.q).mkString(", ")
      ("q" -> queries) ~ ("type" -> decompose(plot.display)) ~ ("style" -> style) ~ ("metadata" -> metadata)
    }
  }))
}

/**
 * Timeseries graph definition.
 *
 * @param plots plots.
 * @param yaxis Y-axis options.
 */
final case class TimeseriesDefinition(plots: Seq[TimeseriesPlot], yaxis: AxisOptions = AxisOptions.Default)
  extends GraphDefinition[Timeseries.type] {

  val visualization = Timeseries

  def withYAxis(axis: AxisOptions): TimeseriesDefinition = copy(yaxis = axis)

  def withYAxis(scale: GraphScale = GraphScale.Default,
                min: Option[BigDecimal] = None,
                max: Option[BigDecimal] = None,
                includeZero: Boolean = true): TimeseriesDefinition = withYAxis(AxisOptions(scale, min, max, includeZero))
}