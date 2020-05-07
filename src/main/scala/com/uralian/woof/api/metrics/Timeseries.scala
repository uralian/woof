package com.uralian.woof.api.metrics

import java.time.{Duration, Instant}

import com.uralian.woof.api._
import com.uralian.woof.util.JsonUtils
import org.json4s.FieldSerializer
import org.json4s.native.Serialization

/**
 * Timeseris data returned by DataDog.
 *
 * @param metric      metric name.
 * @param start       start time.
 * @param end         end time.
 * @param interval    time interval.
 * @param tags        tags specified in "by" clause.
 * @param length      series length.
 * @param queryIndex  query index.
 * @param aggregation aggregation method.
 * @param scope       series scope (filter tags and group-by tags)
 * @param points      data points.
 * @param expression  query expression.
 * @param units       metric units.
 * @param displayName query display name.
 */
final case class Timeseries(metric: String,
                            start: Instant,
                            end: Instant,
                            interval: Duration,
                            tags: Seq[Tag],
                            length: Int,
                            queryIndex: Int,
                            aggregation: String,
                            scope: Scope,
                            points: Seq[Point],
                            expression: String,
                            units: Seq[UnitInfo],
                            displayName: String) {
  override def toString: String = Serialization.write(this)
}

/**
 * Provides JSON serializer for [[Timeseries]] instances.
 */
object Timeseries extends JsonUtils {

  import org.json4s.Extraction._

  private val desStartStop: FDes = {
    case ("start", millis) => "start" -> decompose(millis.extract[Long] / 1000)
    case ("end", millis)   => "end" -> decompose(millis.extract[Long] / 1000)
  }

  private val pairs = List("tags" -> "tag_set", "queryIndex" -> "query_index", "aggregation" -> "aggr",
    "points" -> "pointlist", "units" -> "unit", "displayName" -> "display_name")

  val serializer = FieldSerializer[Timeseries](
    serializer = renameFieldsToJson(pairs: _*),
    deserializer = combine(renameFieldsFromJson(pairs.map(p => p._2 -> p._1): _*), desStartStop)
  )
}