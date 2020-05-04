package com.uralian.woof.api.metrics

import java.time.Instant

import com.uralian.woof.api.{AbstractHttpApi, MetricQuery}
import com.uralian.woof.http.DataDogClient

import scala.concurrent.{ExecutionContext, Future}

/**
 * DataDog Metrics API.
 */
trait MetricsApi {

  /**
   * Returns a list of active metrics from the specified moment in time.
   *
   * @param from moment in time to search for metrics from.
   * @param host if specified, only metrics for the given host are searched.
   * @return a list of active metrics.
   */
  def getActiveMetrics(from: Instant, host: Option[String] = None): Future[Seq[String]]

  /**
   * Searches for metrics.
   *
   * @param query search query.
   * @return a list of metrics satisfying the search criteria.
   */
  def searchMetrics(query: String): Future[Seq[String]]

  /**
   * Updates metric metadata.
   *
   * @param metric metric name.
   * @param md     metadata.
   * @return the new metric metadata.
   */
  def updateMetadata(metric: String, md: MetricMetadata): Future[MetricMetadata]

  /**
   * Retrieves metric metadata.
   *
   * @param metric metric name.
   * @return the metric metadata.
   */
  def getMetadata(metric: String): Future[MetricMetadata]

  /**
   * Creates metric timeseries.
   *
   * @param series a list of timeseries.
   * @return `true` if the operation was successful, `false` otherwise.
   */
  def createSeries(series: Seq[CreateSeries]): Future[Boolean]

  /**
   * Queries timeseries stream.
   *
   * @param query DataDog metric query.
   * @param from  the start time.
   * @param to    the end time.
   * @return a list of timeseries.
   */
  def querySeries(query: MetricQuery, from: Instant, to: Instant = Instant.now): Future[Seq[Timeseries]]
}

/**
 * HTTP-based implementation of [[MetricsApi]].
 *
 * @param client DataDog client.
 * @param ec     execution context.
 */
class MetricsHttpApi(client: DataDogClient)(implicit ec: ExecutionContext)
  extends AbstractHttpApi(client) with MetricsApi {

  private object paths {
    val metric = "v1/metrics"
    val search = "v1/search"
    val series = "v1/series"
    val query = "v1/query"
  }

  def getActiveMetrics(from: Instant, host: Option[String] = None): Future[Seq[String]] = apiGetJ(paths.metric,
    "from" -> from.getEpochSecond, "host" -> host) map { json =>
    (json \ "metrics").extract[Seq[String]]
  }

  def searchMetrics(query: String): Future[Seq[String]] = apiGetJ(paths.search,
    "q" -> s"metrics:$query") map { json =>
    (json \ "results" \ "metrics").extract[Seq[String]]
  }

  def updateMetadata(metric: String, md: MetricMetadata): Future[MetricMetadata] =
    apiPut[MetricMetadata, MetricMetadata](s"${paths.metric}/$metric", md)

  def getMetadata(metric: String): Future[MetricMetadata] = apiGet[MetricMetadata](s"${paths.metric}/$metric")

  def createSeries(series: Seq[CreateSeries]): Future[Boolean] = {
    apiPostJ(paths.series, "series" -> series).map { json =>
      (json \ "status").extract[String] == "ok"
    }
  }

  def querySeries(query: MetricQuery,
                  from: Instant,
                  to: Instant = Instant.now): Future[Seq[Timeseries]] = apiGetJ(paths.query,
    "query" -> query.q, "from" -> from.getEpochSecond, "to" -> to.getEpochSecond) map { json =>
    (json \ "series").extract[Seq[Timeseries]]
  }
}