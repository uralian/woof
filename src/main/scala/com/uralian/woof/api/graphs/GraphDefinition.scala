package com.uralian.woof.api.graphs

import com.uralian.woof.api.graphs.Visualization._
import com.uralian.woof.api._
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

/**
 * QueryTable column.
 *
 * @param metric     metric query.
 * @param prefix     metric prefix.
 * @param suffix     metric suffix.
 * @param aggregator metric aggregator.
 * @param rollup     time rollup.
 * @param alias      column alias.
 * @param formats    conditional formats.
 */
final case class QueryTableColumn(metric: String,
                                  prefix: String = "",
                                  suffix: String = "",
                                  aggregator: MetricAggregator = MetricAggregator.Avg,
                                  rollup: QueryValueAggregator = QueryValueAggregator.Avg,
                                  alias: Option[String] = None,
                                  formats: Seq[ConditionalFormat] = Nil) {

  def wrapIn(prefix: String, suffix: String) = copy(prefix = prefix, suffix = suffix)

  def aggregate(aggregator: MetricAggregator) = copy(aggregator = aggregator)

  def rollup(rollup: QueryValueAggregator) = copy(rollup = rollup)

  def as(alias: String) = copy(alias = Some(alias))

  def withFormats(moreFormats: ConditionalFormat*) = copy(formats = formats ++ moreFormats)

  private[api] def toPlot(rows: Option[DataWindow], scope: Scope, groupBy: Seq[TagName]) = {
    val query = MetricQuery.QueryBuilder(metric, aggregator, scope, groupBy, prefix + _ + suffix)
    QueryTablePlot(query, rollup, rows, alias, formats)
  }
}

/**
 * Query table plot.
 *
 * @param query
 * @param aggregator
 * @param rows
 * @param alias
 * @param formats
 */
private[api] final case class QueryTablePlot(query: MetricQuery,
                                             aggregator: QueryValueAggregator,
                                             rows: Option[DataWindow],
                                             alias: Option[String],
                                             formats: Seq[ConditionalFormat]) extends GraphPlot[QueryTable.type]

/**
 * Factory for [[QueryTablePlot]] instances.
 */
private[api] object QueryTablePlot {

  import Extraction._

  val serializer: CustomSerializer[QueryTablePlot] = new CustomSerializer[QueryTablePlot](_ => ( {
    case _ => ??? //todo implement for Dashboard API responses
  }, {
    case plot: QueryTablePlot => ("q" -> decompose(plot.query)) ~ ("aggregator" -> decompose(plot.aggregator)) ~
      ("alias" -> decompose(plot.alias)) ~ ("conditional_formats" -> decompose(plot.formats)) merge decompose(plot.rows)
  }))
}

/**
 * QueryTable graph definition.
 *
 * @param columns        table column.
 * @param keyColumnIndex the index of the key column.
 * @param rows           row selection.
 * @param scope          metric scope.
 * @param groupBy        metric grouping.
 */
final case class QueryTableDefinition(columns: Seq[QueryTableColumn],
                                      keyColumnIndex: Int = 0,
                                      rows: DataWindow = DataWindow(SortDirection.Descending, 10),
                                      scope: Scope = Scope.All,
                                      groupBy: Seq[TagName] = Nil) extends GraphDefinition[QueryTable.type] {

  require(!columns.isEmpty, "There should be at least one column in the table")
  require(keyColumnIndex < columns.size, "Key column index out of range")

  def withColumns(moreColumns: QueryTableColumn*) = copy(columns = columns ++ moreColumns)

  def withKeyColumn(index: Int) = copy(keyColumnIndex = index)

  def withRows(dir: SortDirection, limit: Int) = copy(rows = DataWindow(dir, limit))

  def filterBy(elements: ScopeElement*) = copy(scope = Scope.Filter(elements: _*))

  def groupBy(items: String*) = copy(groupBy = groupBy ++ items.map(TagName.apply))

  val visualization = QueryTable

  val plots = columns.zipWithIndex map {
    case (col, index) => col.toPlot(if (keyColumnIndex == index) Some(rows) else None, scope, groupBy)
  }
}

/**
 * Factory for [[QueryTableDefinition]] instances.
 */
object QueryTableDefinition extends JsonUtils {

  val serializer = FieldSerializer[QueryTableDefinition](
    combine(ignoreFields("columns", "keyColumnIndex", "rows", "scope", "groupBy"),
      renameFieldsToJson("visualization" -> "viz", "plots" -> "requests"))
  )
}