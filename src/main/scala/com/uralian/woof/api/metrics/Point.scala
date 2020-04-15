package com.uralian.woof.api.metrics

import java.time.Instant

import org.json4s._

/**
 * A data point in time series.
 *
 * @param time  time of the measurement.
 * @param value measurement value.
 */
final case class Point(time: Instant, value: BigDecimal)

/**
 * Factory for [[Point]] instances.
 */
object Point {

  import Extraction._

  /**
   * Creates a new [[Point]] instance.
   *
   * @param seconds number of seconds since Epoch.
   * @param value   value.
   * @return a new [[Point]] instance.
   */
  def apply(seconds: Long, value: BigDecimal): Point = Point(Instant.ofEpochSecond(seconds), value)

  // for whatever reason, DataDog Events API expect time arguments as seconds, but return time values in milliseconds.
  val serializer: CustomSerializer[Point] = new CustomSerializer[Point](_ => ( {
    case JArray(millis :: value :: Nil) => Point(Instant.ofEpochMilli(millis.extract[Long]), value.extract[BigDecimal])
  }, {
    case point: Point => JArray(decompose(point.time) :: decompose(point.value) :: Nil)
  }))
}
