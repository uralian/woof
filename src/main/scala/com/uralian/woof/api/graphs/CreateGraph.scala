package com.uralian.woof.api.graphs

import com.uralian.woof.util.JsonUtils
import org.json4s.FieldSerializer
import org.json4s.native.Serialization

/**
 * Request to create a live embeddable graph.
 *
 * @param graph     graph definition.
 * @param timeframe graph timeframe.
 * @param size      graph size.
 * @param legend    turns on/off the legend.
 * @param title     graph title.
 */
final case class CreateGraph(graph: GraphDefinition[_ <: Visualization],
                             timeframe: Timeframe = Timeframe.Default,
                             size: GraphSize = GraphSize.Default,
                             legend: Boolean = false,
                             title: Option[String] = None) {

  def withTimeframe(tf: Timeframe) = copy(timeframe = tf)

  def withSize(size: GraphSize) = copy(size = size)

  def withLegend = copy(legend = true)

  def withTitle(title: String) = copy(title = Some(title))

  override def toString: String = Serialization.write(this)
}

/**
 * Provides JSON serializer for [[CreateGraph]] instances.
 */
object CreateGraph extends JsonUtils {

  private val ser: FSer = {
    case ("graph", graph)          => Some("graph_json" -> Serialization.write(graph))
    case ("legend", flag: Boolean) => Some("legend" -> (if (flag) "yes" else "no"))
  }

  val serializer = FieldSerializer[CreateGraph](serializer = ser)
}