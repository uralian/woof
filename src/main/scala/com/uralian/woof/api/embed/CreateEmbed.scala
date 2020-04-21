package com.uralian.woof.api.embed

import com.uralian.woof.util.JsonUtils
import org.json4s.FieldSerializer
import org.json4s.native.Serialization

/**
 * Request to create an embeddable graph.
 *
 * @param queries   a list of queries.
 * @param timeframe graph timeframe.
 * @param size      graph size.
 * @param legend    turns on/off the legend.
 * @param title     graph title.
 */
final case class CreateEmbed(queries: Seq[String],
                             timeframe: Timeframe = Timeframe.Default,
                             size: GraphSize = GraphSize.Default,
                             legend: Boolean = false,
                             title: Option[String] = None) {

  def withQueries(moreQueries: String*) = copy(queries = this.queries ++ moreQueries)

  def withTimeframe(tf: Timeframe) = copy(timeframe = tf)

  def withSize(size: GraphSize) = copy(size = size)

  def withLegend = copy(legend = true)

  def withTitle(title: String) = copy(title = Some(title))

  override def toString: String = Serialization.write(this)
}

/**
 * Provides JSON serializer for [[CreateEmbed]] instances.
 */
object CreateEmbed extends JsonUtils {

  private val serRequests: FSer = {
    case ("queries", queries: Seq[_]) =>
      val data = Map("viz" -> "timeseries", "events" -> Nil, "requests" -> queries.map(r => Map("q" -> r.toString)))
      Some("graph_json" -> Serialization.write(data))
  }

  private val serLegend: FSer = {
    case ("legend", flag: Boolean) => Some("legend" -> (if (flag) "yes" else "no"))
  }

  val serializer = FieldSerializer[CreateEmbed](serializer = combine(serLegend, serRequests))

  /**
   * Creates a new [[CreateEmbed]] instance.
   *
   * @param queries queries to run.
   * @return a new [[CreateEmbed]] instance.
   */
  def apply(queries: String*): CreateEmbed = new CreateEmbed(queries)
}
