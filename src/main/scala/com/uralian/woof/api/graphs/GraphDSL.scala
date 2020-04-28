package com.uralian.woof.api.graphs

import com.uralian.woof.api.MetricQuery

/**
 * DataDog graph DSL.
 */
object GraphDSL {

  /**
   * Adds functionality to MetricQuery.
   *
   * @param underlying metric query.
   */
  implicit class RichMetricQuery(val underlying: MetricQuery) extends AnyVal {
    /**
     * Adds an alias to metric query.
     *
     * @param alias query alias.
     * @return a pair MetricQuery->Some(alias).
     */
    def as(alias: String): (MetricQuery, Option[String]) = underlying -> Some(alias)
  }

  /**
   * Implicitly converts a metric query into a metric query w/o alias construct.
   *
   * @param query metric query.
   * @return a pair MetricQuery->None.
   */
  implicit def queryToQueryAlias(query: MetricQuery): (MetricQuery, Option[String]) = query -> None

  /**
   * Creates a new Timeseries graph definition.
   *
   * @param plots a list of Timeseries plots.
   * @return a new TimeseriesDefinition instance.
   */
  def timeseries(plots: TimeseriesPlot*) = TimeseriesDefinition(plots)

  /**
   * Creates a new Timeseries plot.
   *
   * @param queries plot queries.
   * @return a new TimeseriesPlot instance.
   */
  def plot(queries: (MetricQuery, Option[String])*) = TimeseriesPlot(queries)
}
