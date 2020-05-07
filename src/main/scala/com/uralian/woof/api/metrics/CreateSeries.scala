package com.uralian.woof.api.metrics

import java.time.Duration

import com.uralian.woof.api.Tag
import com.uralian.woof.api.metrics.MetricScale._
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.Serialization

/**
 * A request to create a new metric series in DataDog.
 *
 * @param metric metric name.
 * @param points list of data points.
 * @param scale  metric scale.
 * @param host   host associated with this series.
 * @param tags   tags.
 */
final case class CreateSeries(metric: String,
                              points: Seq[Point],
                              scale: MetricScale = GaugeScale,
                              host: Option[String] = None,
                              tags: Seq[Tag] = Nil) {

  def withPoints(morePoints: Point*) = copy(points = points ++ morePoints)

  def withScale(scale: MetricScale) = copy(scale = scale)

  def asGauge = withScale(GaugeScale)

  def asCount(interval: Duration) = withScale(CountScale(Some(interval)))

  def asRate = withScale(RateScale(None))

  def asRate(interval: Duration) = withScale(RateScale(Some(interval)))

  def withHost(host: String) = copy(host = Some(host))

  def withTags(moreTags: Tag*) = copy(tags = tags ++ moreTags)

  override def toString: String = Serialization.write(this)
}

/**
 * Factory for [[CreateSeries]] instances.
 */
object CreateSeries {

  // deserializer not needed, this is a "one way" request class
  val serializer: CustomSerializer[CreateSeries] = new CustomSerializer[CreateSeries](_ => ( {
    case jv => ???
  }, {
    case CreateSeries(metric, points, scale, host, tags) => ("metric" -> metric) ~
      ("points" -> points.map(Extraction.decompose)) ~
      ("host" -> host) ~
      ("tags" -> tags.map(_.toString)) merge Extraction.decompose(scale)
  }))

  /**
   * Creates a series request with a single data point.
   *
   * @param metric metric name.
   * @param point  data point.
   * @return a new series request.
   */
  def apply(metric: String, point: Point): CreateSeries = apply(metric, List(point))
}