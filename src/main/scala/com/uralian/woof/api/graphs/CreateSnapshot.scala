package com.uralian.woof.api.graphs

import java.time.Instant

import com.uralian.woof.api.{MetricQuery, QueryParams}
import com.uralian.woof.util.JsonUtils
import org.json4s.FieldSerializer
import org.json4s.native.Serialization

/**
 * Request to create a graph snapshot.
 *
 * @param config either a metric query or full graph definition.
 * @param start  start time.
 * @param end    end time.
 * @param title  graph title.
 */
final case class CreateSnapshot(config: Either[MetricQuery, GraphDefinition[_ <: Visualization]],
                                start: Instant,
                                end: Instant,
                                title: Option[String] = None) extends QueryParams {

  def withTitle(str: String) = copy(title = Some(str))

  override def toString: String = Serialization.write(this)
}

/**
 * A factory for [[CreateSnapshot]] instances.
 */
object CreateSnapshot extends JsonUtils {

  def apply(query: MetricQuery, start: Instant, end: Instant): CreateSnapshot =
    apply(Left(query), start, end)

  def apply(graph: GraphDefinition[_ <: Visualization], start: Instant, end: Instant): CreateSnapshot =
    apply(Right(graph), start, end)

  private val ser: FSer = {
    case ("config", Left(query: MetricQuery))         => Some("metric_query" -> query.q)
    case ("config", Right(graph: GraphDefinition[_])) => Some("graph_def" -> Serialization.write(graph))
  }

  val serializer = FieldSerializer[CreateSnapshot](serializer = ser)
}