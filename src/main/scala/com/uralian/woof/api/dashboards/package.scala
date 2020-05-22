package com.uralian.woof.api

import com.uralian.woof.api.graphs.{AxisOptions, ChangeDefinition, ChangePlot, ColorPalette, DistributionDefinition, DistributionPlot, HeatmapDefinition, HeatmapPlot, HostmapDefinition, HostmapPlot, HostmapStyle, TimeseriesDefinition, TimeseriesPlot}
import com.uralian.woof.util.JsonUtils
import enumeratum.Json4s
import org.json4s.JsonDSL._
import org.json4s.{FieldSerializer, JNull}

/**
 * Helper methods and types for Dashboards API.
 */
package object dashboards extends JsonUtils {

  private val axisOptionsSerializer = FieldSerializer[AxisOptions](
    {
      case ("min", Some(x))   => Some("min" -> x.toString)
      case ("max", Some(x))   => Some("max" -> x.toString)
      case ("includeZero", x) => Some("include_zero" -> x)
    }
  )

  private val tsPlotSerializer = customSerializer[TimeseriesPlot](toJson = implicit fmt => {
    case plot: TimeseriesPlot => {
      val style = Map("palette" -> plot.palette, "line_type" -> plot.lineType, "line_width" -> plot.stroke)
      val metadata = Option(plot.queries.collect {
        case (query, Some(alias)) => Map("expression" -> query.q, "alias_name" -> alias)
      }).filterNot(_.isEmpty)
      val queries = plot.queries.map(_._1.q).mkString(", ")
      ("q" -> queries) ~ ("display_type" -> plot.display) ~ ("style" -> style) ~ ("metadata" -> metadata)
    }
  })

  private val tsDefSerializer = translateFields[TimeseriesDefinition]("visualization" -> "type",
    "plots" -> "requests", "showLegend" -> "show_legend")

  private val changePlotSerializer = {
    val ser: FSer = {
      case ("absolute", true)  => Some("change_type" -> "absolute")
      case ("absolute", false) => Some("change_type" -> "relative")
    }
    FieldSerializer[ChangePlot](combine(
      ignoreFields("metric", "aggregator", "scope", "groupBy"),
      renameFieldsToJson("compareTo" -> "compare_to", "sortBy" -> "order_by", "sortDirection" -> "order_dir",
        "increaseGood" -> "increase_good", "includePresent" -> "show_present"
      ),
      ser
    ))
  }

  private val changeDefSerializer = translateFields[ChangeDefinition]("visualization" -> "type",
    "plots" -> "requests", "plot" -> null)

  private val distPlotSerializer = {
    val ser: FSer = {
      case ("queries", queries: Seq[_])       => Some("q" -> queries.map(_.asInstanceOf[MetricQuery].q).mkString(", "))
      case ("palette", palette: ColorPalette) => Some("style" -> ("palette" -> palette.entryName))
    }
    FieldSerializer[DistributionPlot](serializer = ser)
  }

  private val distDefSerializer = translateFields[DistributionDefinition]("visualization" -> "type",
    "plots" -> "requests", "plot" -> null, "showLegend" -> "show_legend")

  private val heatPlotSerializer = {
    val ser: FSer = {
      case ("queries", queries: Seq[_])       => Some("q" -> queries.map(_.asInstanceOf[MetricQuery].q).mkString(", "))
      case ("palette", palette: ColorPalette) => Some("style" -> ("palette" -> palette.entryName))
    }
    FieldSerializer[HeatmapPlot](serializer = ser)
  }

  private val heatDefSerializer = translateFields[HeatmapDefinition]("visualization" -> "type",
    "plots" -> "requests", "plot" -> null, "showLegend" -> "show_legend")

  private val hostmapStyleSerializer = {
    val ser: FSer = {
      case ("min", Some(min)) => Some("fill_min" -> min.toString)
      case ("max", Some(max)) => Some("fill_max" -> max.toString)
    }
    FieldSerializer[HostmapStyle](combine(
      renameFieldsToJson("flip" -> "palette_flip"),
      ser
    ))
  }

  private val hostmapDefSerializer = {
    val ser: FSer = {
      case ("scope", Scope.All)                                         => Some("scope" -> JNull)
      case ("scope", f: Scope.Filter)                                   => Some("scope" -> f.elements)
      case ("plots", (fill: HostmapPlot) :: (size: HostmapPlot) :: Nil) => Some("requests" ->
        Map("fill" -> Map("q" -> fill.q), "size" -> Map("q" -> size.q))
      )
      case ("plots", (fill: HostmapPlot) :: Nil)                        => Some("requests" ->
        Map("fill" -> Map("q" -> fill.q))
      )
    }
    FieldSerializer[HostmapDefinition](combine(
      renameFieldsToJson("visualization" -> "type", "groupBy" -> "group",
        "noGroupHosts" -> "no_group_hosts", "noMetricHosts" -> "no_metric_hosts", "nodeType" -> "node_type"),
      ignoreFields("fill", "size"),
      ser
    ))
  }

  implicit val dashboardFormats = apiFormats ++ com.uralian.woof.api.graphs.graphEnumSerializers +
    axisOptionsSerializer +
    tsPlotSerializer +
    tsDefSerializer +
    changePlotSerializer +
    changeDefSerializer +
    distPlotSerializer +
    distDefSerializer +
    heatPlotSerializer +
    heatDefSerializer +
    hostmapStyleSerializer +
    hostmapDefSerializer +
    Json4s.serializer(LayoutType) +
    Preset.serializer +
    Widget.serializer +
    WidgetGroup.serializer +
    CreateDashboard.serializer +
    Dashboard.serializer
}
