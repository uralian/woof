package com.uralian.woof.api.graphs

import com.uralian.woof.api.MetricQuery
import com.uralian.woof.api.graphs.Visualization._
import com.uralian.woof.util.JsonUtils
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

/**
 * Factory for [[TimeseriesDefinition]] instances.
 */
object TimeseriesDefinition {
  val serializer = translateFields[TimeseriesDefinition]("visualization" -> "viz", "plots" -> "requests")
}

/**
 * QueryValue plot.
 *
 * @param query      metric query.
 * @param aggregator aggregator.
 * @param formats    conditional formats.
 */
final case class QueryValuePlot(query: MetricQuery,
                                aggregator: QueryValueAggregator = QueryValueAggregator.Default,
                                formats: Seq[ConditionalFormat] = Nil) extends GraphPlot[QueryValue.type] {

  def aggregate(aggregator: QueryValueAggregator) = copy(aggregator = aggregator)

  def withFormats(moreFormats: ConditionalFormat*) = copy(formats = formats ++ moreFormats)
}

/**
 * Factory for [[QueryValuePlot]] instances.
 */
object QueryValuePlot extends JsonUtils {
  val serializer = translateFields[QueryValuePlot]("query" -> "q", "formats" -> "conditional_formats")
}

/**
 * QueryValue graph definition.
 *
 * @param plot query value plot.
 * @param autoscale
 * @param customUnit
 * @param precision
 * @param textAlign
 */
final case class QueryValueDefinition(plot: QueryValuePlot,
                                      autoscale: Boolean = true,
                                      customUnit: Option[String] = None,
                                      precision: Option[Int] = None,
                                      textAlign: TextAlign = TextAlign.Center)
  extends GraphDefinition[QueryValue.type] {

  def notAutoscaled = copy(autoscale = false)

  def withCustomUnit(unit: String) = copy(customUnit = Some(unit))

  def withPrecision(places: Int) = copy(precision = Some(places))

  def withAlign(ta: TextAlign) = copy(textAlign = ta)

  val visualization = QueryValue

  val plots = Seq(plot)
}

/**
 * Factory for [[QueryValueDefinition]] instances.
 */
object QueryValueDefinition {

  val serializer = FieldSerializer[QueryValueDefinition](serializer = combine(
    renameFieldsToJson("visualization" -> "viz", "plots" -> "requests", "customUnit" -> "custom_unit",
      "textAlign" -> "text_align"),
    {
      case ("plot", _) => None
    }
  ))
}