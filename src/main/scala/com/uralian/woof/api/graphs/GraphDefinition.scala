package com.uralian.woof.api.graphs

import com.uralian.woof.api._
import com.uralian.woof.api.dashboards.WidgetContent
import com.uralian.woof.api.graphs.Visualization._
import com.uralian.woof.util.JsonUtils._
import org.json4s.JsonDSL._
import org.json4s._

/**
 * DataDog graph definition.
 *
 * @tparam V visualization type.
 */
sealed trait GraphDefinition[V <: Visualization] extends WidgetContent {

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
final case class TimeseriesPlot(queries: Seq[QueryWithAlias],
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
 * @param plots      plots.
 * @param yaxis      Y-axis options.
 * @param title      graph title (for dashboards).
 * @param showLegend shows the legend (for screenboards only).
 */
final case class TimeseriesDefinition(plots: Seq[TimeseriesPlot],
                                      yaxis: AxisOptions = AxisOptions.Default,
                                      title: Option[String] = None,
                                      showLegend: Boolean = false)
  extends GraphDefinition[Timeseries.type] {

  val visualization = Timeseries

  def withTitle(title: String) = copy(title = Some(title))

  def withLegend = copy(showLegend = true)

  def withYAxis(axis: AxisOptions): TimeseriesDefinition = copy(yaxis = axis)
}

/**
 * Factory for [[TimeseriesDefinition]] instances.
 */
object TimeseriesDefinition {
  val serializer = translateFields[TimeseriesDefinition]("visualization" -> "viz", "plots" -> "requests",
    "showLegend" -> null)
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
object QueryValuePlot {
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
 * @param prefix     query prefix.
 * @param suffix     query suffix.
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
    val queryStr = prefix + MetricQuery.QueryBuilder(metric, aggregator, scope, groupBy).q + suffix
    QueryTablePlot(MetricQuery.direct(queryStr) -> alias, rollup, rows, formats)
  }
}

/**
 * Query table plot.
 *
 * @param query      metric query with an optional alias.
 * @param aggregator aggregator.
 * @param rows       row selection.
 * @param formats    conditional formats.
 */
private[api] final case class QueryTablePlot(query: QueryWithAlias,
                                             aggregator: QueryValueAggregator,
                                             rows: Option[DataWindow],
                                             formats: Seq[ConditionalFormat]) extends GraphPlot[QueryTable.type]

/**
 * Factory for [[QueryTablePlot]] instances.
 */
private[api] object QueryTablePlot {

  import Extraction._

  val serializer: CustomSerializer[QueryTablePlot] = new CustomSerializer[QueryTablePlot](_ => ( {
    case _ => ??? //todo implement for Dashboard API responses
  }, {
    case plot: QueryTablePlot => ("q" -> decompose(plot.query._1)) ~ ("aggregator" -> decompose(plot.aggregator)) ~
      ("alias" -> decompose(plot.query._2)) ~ ("conditional_formats" -> decompose(plot.formats)) merge decompose(plot.rows)
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

  def withKeyColumn(index: Int) = copy(keyColumnIndex = index)

  def withRows(dir: SortDirection, limit: Int) = copy(rows = DataWindow(dir, limit))

  def filterBy(elements: ScopeElement*) = copy(scope = Scope.Filter(elements: _*))

  def groupBy(names: String*) = copy(groupBy = groupBy ++ names.map(TagName.apply))

  val visualization = QueryTable

  val plots = columns.zipWithIndex map {
    case (col, index) => col.toPlot(if (keyColumnIndex == index) Some(rows) else None, scope, groupBy)
  }
}

/**
 * Factory for [[QueryTableDefinition]] instances.
 */
object QueryTableDefinition {

  val serializer = FieldSerializer[QueryTableDefinition](
    combine(ignoreFields("columns", "keyColumnIndex", "rows", "scope", "groupBy"),
      renameFieldsToJson("visualization" -> "viz", "plots" -> "requests"))
  )
}

/**
 * Heatmap plot.
 *
 * @param queries metric queries.
 * @param palette color palette.
 */
final case class HeatmapPlot(queries: Seq[MetricQuery],
                             palette: ColorPalette = ColorPalette.Default) extends GraphPlot[Heatmap.type] {

  def withPalette(palette: ColorPalette) = copy(palette = palette)
}

/**
 * Factory for [[HeatmapPlot]] instances.
 */
object HeatmapPlot {

  import Extraction._

  val serializer: CustomSerializer[HeatmapPlot] = new CustomSerializer[HeatmapPlot](_ => ( {
    case _ => ??? //todo implement for Dashboard API responses
  }, {
    case plot: HeatmapPlot => {
      val style = ("palette" -> decompose(plot.palette)) ~ ("type" -> "solid") ~ ("width" -> "normal")
      val queries = plot.queries.map(_.q).mkString(", ")
      ("q" -> queries) ~ ("style" -> style)
    }
  }))
}

/**
 * Heatmap graph definition.
 *
 * @param plot       heatmap plot.
 * @param yaxis      Y-axis options.
 * @param title      graph title (for dashboards).
 * @param showLegend show graph legend (for screenboards only)
 */
final case class HeatmapDefinition(plot: HeatmapPlot,
                                   yaxis: AxisOptions = AxisOptions.Default,
                                   title: Option[String] = None,
                                   showLegend: Boolean = false) extends GraphDefinition[Heatmap.type] {

  def withYAxis(axis: AxisOptions): HeatmapDefinition = copy(yaxis = axis)

  def withTitle(title: String) = copy(title = Some(title))

  def withLegend = copy(showLegend = true)

  val visualization = Heatmap

  val plots = Seq(plot)
}

/**
 * Factory for [[HeatmapDefinition]] instances.
 */
object HeatmapDefinition {
  val serializer = FieldSerializer[HeatmapDefinition](combine(
    renameFieldsToJson("visualization" -> "viz", "plots" -> "requests"),
    ignoreFields("plot", "showLegend")
  ))
}

/**
 * Scatter plot axis.
 *
 * @param metric     metric name.
 * @param prefix     query prefix.
 * @param suffix     query suffix.
 * @param scope      query scope.
 * @param aggregator metric aggregator.
 * @param rollup     metric rollup.
 * @param options    axis options.
 */
final case class ScatterAxis(metric: String,
                             prefix: String = "",
                             suffix: String = "",
                             scope: Scope = Scope.All,
                             aggregator: MetricAggregator = MetricAggregator.Avg,
                             rollup: QueryValueAggregator = QueryValueAggregator.Avg,
                             options: AxisOptions = AxisOptions.Default) {

  def wrapIn(prefix: String, suffix: String) = copy(prefix = prefix, suffix = suffix)

  def filterBy(elements: ScopeElement*) = copy(scope = Scope.Filter(elements: _*))

  def aggregate(aggregator: MetricAggregator) = copy(aggregator = aggregator)

  def rollup(rollup: QueryValueAggregator) = copy(rollup = rollup)

  def withOptions(opt: AxisOptions): ScatterAxis = copy(options = opt)

  def withOptions(label: Option[String] = None,
                  scale: GraphScale = GraphScale.Default,
                  min: Option[BigDecimal] = None,
                  max: Option[BigDecimal] = None,
                  includeZero: Boolean = true): ScatterAxis = withOptions(AxisOptions(label, scale, min, max, includeZero))

  private[api] def toPlot(pointBy: Seq[TagName], colorBy: Seq[TagName]) = {
    val queryStr = prefix + MetricQuery.QueryBuilder(metric, aggregator, scope, pointBy ++ colorBy).q + suffix
    ScatterPlot(MetricQuery.direct(queryStr), rollup)
  }
}

/**
 * Scatter plot.
 *
 * @param query      metric query.
 * @param aggregator query aggregator.
 */
private[api] final case class ScatterPlot(query: MetricQuery, aggregator: QueryValueAggregator) extends GraphPlot[Scatter.type]

/**
 * Factory for [[ScatterPlot]] instances.
 */
private[api] object ScatterPlot {

  import Extraction._

  val serializer: CustomSerializer[ScatterPlot] = new CustomSerializer[ScatterPlot](_ => ( {
    case _ => ??? //todo implement for Dashboard API responses
  }, {
    case plot: ScatterPlot => ("q" -> decompose(plot.query)) ~ ("aggregator" -> decompose(plot.aggregator))
  }))
}

/**
 * Scatter graph definition.
 *
 * @param x       X-axis definition.
 * @param y       Y-axis definition.
 * @param pointBy point by tag names.
 * @param colorBy color by tag names.
 */
final case class ScatterDefinition(x: ScatterAxis,
                                   y: ScatterAxis,
                                   pointBy: Seq[TagName] = Nil,
                                   colorBy: Seq[TagName] = Nil) extends GraphDefinition[Scatter.type] {

  def pointBy(names: String*): ScatterDefinition = copy(pointBy = pointBy ++ names.map(TagName.apply))

  def colorBy(names: String*): ScatterDefinition = copy(colorBy = colorBy ++ names.map(TagName.apply))

  val visualization = Scatter

  val plots = List(x.toPlot(pointBy, colorBy), y.toPlot(pointBy, colorBy))
}

/**
 * Factory for [[ScatterDefinition]] instances.
 */
object ScatterDefinition {

  import Extraction._

  private val ser: FSer = {
    case ("plots", x :: y :: Nil) => Some("requests" -> ("x" -> decompose(x)) ~ ("y" -> decompose(y)))
    case ("x", x: ScatterAxis)    => Some("xaxis" -> decompose(x.options))
    case ("y", y: ScatterAxis)    => Some("yaxis" -> decompose(y.options))
  }

  val serializer = FieldSerializer[ScatterDefinition](combine(
    ignoreFields("pointBy"),
    renameFieldsToJson("colorBy" -> "color_by_groups", "visualization" -> "viz"),
    ser
  ))
}

/**
 * Distribution plot.
 *
 * @param queries metric queries.
 * @param palette color palette.
 */
final case class DistributionPlot(queries: Seq[MetricQuery],
                                  palette: ColorPalette = ColorPalette.Default) extends GraphPlot[Distribution.type] {

  def withPalette(palette: ColorPalette) = copy(palette = palette)
}

/**
 * Factory for [[DistributionPlot]] instances.
 */
object DistributionPlot {

  private val ser: FSer = {
    case ("queries", queries: Seq[_])       => Some("q" -> queries.map(_.asInstanceOf[MetricQuery].q).mkString(", "))
    case ("palette", palette: ColorPalette) => Some("style" ->
      ("palette" -> palette.entryName) ~ ("type" -> "solid") ~ ("width" -> "normal")
    )
  }

  val serializer = FieldSerializer[DistributionPlot](serializer = ser)
}

/**
 * Distribution graph definition.
 *
 * @param plot       distribution plot.
 * @param title      graph title (for dashboards).
 * @param showLegend shows the legend (for screenboards only).
 */
final case class DistributionDefinition(plot: DistributionPlot,
                                        title: Option[String] = None,
                                        showLegend: Boolean = false) extends GraphDefinition[Distribution.type] {

  def withTitle(title: String) = copy(title = Some(title))

  def withLegend = copy(showLegend = true)

  val visualization = Distribution

  val plots = Seq(plot)
}

/**
 * Factory for [[DistributionDefinition]] instances.
 */
object DistributionDefinition {
  val serializer = FieldSerializer[DistributionDefinition](combine(
    renameFieldsToJson("visualization" -> "viz", "plots" -> "requests"),
    ignoreFields("plot", "showLegend")
  ))
}

/**
 * Toplist plost.
 *
 * @param query      metric query.
 * @param rows       row selection.
 * @param aggregator aggregator.
 * @param formats    conditional formats.
 */
final case class ToplistPlot(query: MetricQuery,
                             rows: DataWindow = DataWindow(SortDirection.Descending, 10),
                             aggregator: RankAggregator = RankAggregator.Default,
                             formats: Seq[ConditionalFormat] = Nil) extends GraphPlot[Toplist.type] {

  def withRows(dir: SortDirection, limit: Int) = copy(rows = DataWindow(dir, limit))

  def aggregate(aggregator: RankAggregator) = copy(aggregator = aggregator)

  def withFormats(moreFormats: ConditionalFormat*) = copy(formats = formats ++ moreFormats)

  private val q = s"top(${query.q}, ${rows.limit}, '${aggregator.entryName}', '${rows.order.entryName}')"
}

/**
 * Factory for [[ToplistPlot]] instances.
 */
object ToplistPlot {

  val serializer = FieldSerializer[ToplistPlot](combine(
    renameFieldsToJson("formats" -> "conditional_formats"),
    ignoreFields("query", "rows", "aggregator")
  ))
}

/**
 * Toplist graph definition.
 *
 * @param plot toplist plot.
 */
final case class ToplistDefinition(plot: ToplistPlot) extends GraphDefinition[Toplist.type] {
  val visualization = Toplist
  val plots = Seq(plot)
}

/**
 * Factory for [[ToplistDefinition]] instances.
 */
object ToplistDefinition {
  val serializer = FieldSerializer[ToplistDefinition](combine(
    renameFieldsToJson("visualization" -> "viz", "plots" -> "requests"),
    ignoreFields("plot")
  ))
}

/**
 * Change plot.
 *
 * @param metric         metric name.
 * @param aggregator     metric aggregator.
 * @param scope          list of tags to filter by.
 * @param groupBy        list of tag names to group by.
 * @param compareTo      time base for comparison.
 * @param sortBy         sorting criteria.
 * @param sortDirection  sorting direction.
 * @param increaseGood   whether the increase is good.
 * @param absolute       whether to show the absolute or relative value.
 * @param includePresent whether to include the present value.
 */
final case class ChangePlot(metric: String,
                            aggregator: MetricAggregator = MetricAggregator.Avg,
                            scope: Seq[Tag] = Nil,
                            groupBy: Seq[TagName] = Nil,
                            compareTo: TimeBase = TimeBase.Default,
                            sortBy: ChangeOrder = ChangeOrder.Default,
                            sortDirection: SortDirection = SortDirection.Descending,
                            increaseGood: Boolean = true,
                            absolute: Boolean = true,
                            includePresent: Boolean = false) extends GraphPlot[Change.type] {

  def aggregate(aggregator: MetricAggregator) = copy(aggregator = aggregator)

  def filterBy(tags: Tag*) = copy(scope = scope ++ tags)

  def groupBy(names: String*): ChangePlot = copy(groupBy = groupBy ++ names.map(TagName.apply))

  def compareTo(tb: TimeBase): ChangePlot = copy(compareTo = tb)

  def sortBy(order: ChangeOrder, dir: SortDirection): ChangePlot = copy(sortBy = order, sortDirection = dir)

  def increaseIsBetter = copy(increaseGood = true)

  def decreaseIsBetter = copy(increaseGood = false)

  def showAbsolute = copy(absolute = true)

  def showRelative = copy(absolute = false)

  def showPresent = copy(includePresent = true)

  private val q = MetricQuery.metric(metric).aggregate(aggregator).filterBy(scope: _*).groupBy(groupBy.map(_.toString): _*)
}

/**
 * Factory for [[ChangePlot]] instances.
 */
object ChangePlot {

  private val ser: FSer = {
    case ("absolute", true)        => Some("change_type" -> "absolute")
    case ("absolute", false)       => Some("change_type" -> "relative")
    case ("includePresent", true)  => Some("extra_col" -> "present")
    case ("includePresent", false) => Some("extra_col" -> "")
  }

  val serializer = FieldSerializer[ChangePlot](combine(
    ignoreFields("metric", "aggregator", "scope", "groupBy"),
    renameFieldsToJson("compareTo" -> "compare_to", "sortBy" -> "order_by", "sortDirection" -> "order_dir",
      "increaseGood" -> "increase_good"
    ),
    ser
  ))
}

/**
 * Change graph definition.
 *
 * @param plot  change plot.
 * @param title graph title (for dashboards).
 */
final case class ChangeDefinition(plot: ChangePlot, title: Option[String] = None) extends GraphDefinition[Change.type] {

  def withTitle(title: String) = copy(title = Some(title))

  val visualization = Change
  val plots = Seq(plot)
}

/**
 * Factory for [[ChangeDefinition]] instances.
 */
object ChangeDefinition {
  val serializer = FieldSerializer[ChangeDefinition](combine(
    renameFieldsToJson("visualization" -> "viz", "plots" -> "requests"),
    ignoreFields("plot")
  ))
}

/**
 * Hostmap graph style settings.
 *
 * @param palette color palette.
 * @param flip    whether the palette needs to be switched.
 * @param min     minimum value.
 * @param max     maximum value.
 */
final case class HostmapStyle(palette: HostmapPalette = HostmapPalette.GreenOrange,
                              flip: Boolean = false,
                              min: Option[BigDecimal] = None,
                              max: Option[BigDecimal] = None)

/**
 * Provides JSON serializer for [[HostmapStyle]].
 */
object HostmapStyle {
  val serializer = translateFields[HostmapStyle]("flip" -> "paletteFlip", "min" -> "fillMin", "max" -> "fillMax")
}

/**
 * Hostmap plot.
 *
 * @param `type` plot type ("fill" or "size").
 * @param q      metric query.
 */
private[api] final case class HostmapPlot(`type`: String, q: MetricQuery) extends GraphPlot[Hostmap.type]

/**
 * Hostmap graph definition.
 *
 * @param fill          fill: metric name -> aggregator.
 * @param size          size: metric name -> aggregator.
 * @param scope         query scope.
 * @param groupBy       metric grouping.
 * @param style         graph style.
 * @param noGroupHosts  whether to include hosts without grouping tags.
 * @param noMetricHosts whether to include hosts without metric values.
 */
final case class HostmapDefinition(fill: (String, MetricAggregator),
                                   size: Option[(String, MetricAggregator)] = None,
                                   scope: Scope = Scope.All,
                                   groupBy: Seq[TagName] = Nil,
                                   style: HostmapStyle = HostmapStyle(),
                                   noGroupHosts: Boolean = true,
                                   noMetricHosts: Boolean = true)
  extends GraphDefinition[Hostmap.type] {

  private val nodeType = "host"

  def filterBy(elements: ScopeElement*) = copy(scope = Scope.Filter(elements: _*))

  def groupBy(names: String*) = copy(groupBy = groupBy ++ names.map(TagName.apply))

  def withPalette(palette: HostmapPalette) = copy(style = style.copy(palette = palette))

  def filpped = copy(style = style.copy(flip = true))

  def withMin(num: BigDecimal) = copy(style = style.copy(min = Some(num)))

  def withMax(num: BigDecimal) = copy(style = style.copy(max = Some(num)))

  def hideNoGroupHosts = copy(noGroupHosts = false)

  def hideNoMetricHosts = copy(noMetricHosts = false)

  val visualization = Hostmap

  val plots = List(HostmapPlot("fill", MetricQuery.QueryBuilder(fill._1, fill._2, scope, groupBy))) ++ size.map { sz =>
    HostmapPlot("size", MetricQuery.QueryBuilder(sz._1, sz._2, scope, groupBy))
  }
}

/**
 * Factory for [[HostmapDefinition]] instances.
 */
object HostmapDefinition {

  private val ser: FSer = {
    case ("scope", Scope.All)       => Some("scope" -> JNull)
    case ("scope", f: Scope.Filter) => Some("scope" -> f.elements)
  }

  val serializer = FieldSerializer[HostmapDefinition](combine(
    renameFieldsToJson("visualization" -> "viz", "plots" -> "requests", "groupBy" -> "group"),
    ignoreFields("fill", "size"),
    ser
  ))
}