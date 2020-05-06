package com.uralian.woof.api

import com.uralian.woof.api.MetricQuery.BinOpQuery
import enumeratum.EnumEntry.Lowercase
import enumeratum._
import org.json4s._

/**
 * Metric aggregator used in queries.
 */
sealed trait MetricAggregator extends EnumEntry with Lowercase

/**
 * Available metric aggregators.
 */
object MetricAggregator extends Enum[MetricAggregator] {

  case object Avg extends MetricAggregator

  case object Max extends MetricAggregator

  case object Min extends MetricAggregator

  case object Sum extends MetricAggregator

  val values = findValues
}

/**
 * Query used in metric requests.
 */
sealed trait MetricQuery {
  /**
   * Renders the query.
   *
   * @return the string representation of the query.
   */
  def q: String

  /**
   * Combines this query with another one via "+".
   *
   * @param q2 second query.
   * @return compound query that evaluates to string this.r+q2.r
   */
  def +(q2: MetricQuery): BinOpQuery = combine("+", q2)

  /**
   * Combines this query with another one via "-".
   *
   * @param q2 second query.
   * @return compound query that evaluates to string this.r-q2.r
   */
  def -(q2: MetricQuery) = combine("-", q2)

  /**
   * Combines this query with another one via "*".
   *
   * @param q2 second query.
   * @return compound query that evaluates to string this.r*q2.r
   */
  def *(q2: MetricQuery) = combine("*", q2)

  /**
   * Combines this query with another one via "/".
   *
   * @param q2 second query.
   * @return compound query that evaluates to string this.r/q2.r
   */
  def /(q2: MetricQuery) = combine("/", q2)

  /**
   * Combines this query with another one via an arbitrary separator.
   *
   * @param separator separator string.
   * @param q2        second query.
   * @return compound query that evaluates to string (this.r)(separator)(q2.r).
   */
  def combine(separator: String, q2: MetricQuery) = BinOpQuery(this, separator, q2)

  /**
   * Adds an alias to metric query.
   *
   * @param alias query alias.
   * @return a pair MetricQuery->Some(alias).
   */
  def as(alias: String): QueryWithAlias = this -> Some(alias)
}

/**
 * Factory for [[MetricQuery]] instances.
 */
object MetricQuery {

  /**
   * Creates a freeform query.
   *
   * @param str query string.
   * @return a new freeform query.
   */
  def direct(str: String): FreeformQuery = FreeformQuery(str)

  /**
   * Creates a new query builder based on the supplied metric.
   *
   * @param metricName metric name.
   * @return a new query builder.
   */
  def metric(metricName: String): QueryBuilder = QueryBuilder(metric = metricName)

  /**
   * Creates a function wrapper around a list of queries.
   *
   * @param fName   name of the function.
   * @param queries function arguments.
   * @return a new function query.
   */
  def function(fName: String)(queries: MetricQuery*): FuncQuery = FuncQuery(fName, queries: _*)

  /**
   * Combines two queries together via a separator, which can be "*", "/", etc.
   * Examples:
   * {{{
   *   -  avg:system.mem.used{*} / avg:system.mem.total{*}
   *   -  max:system.cpu.user * 100
   * }}}
   *
   * @param q1        first query.
   * @param separator binary operator.
   * @param q2        second query.
   */
  final case class BinOpQuery(q1: MetricQuery, separator: String, q2: MetricQuery) extends MetricQuery {
    val q: String = s"${q1.q}$separator${q2.q}"
  }

  /**
   * Creates a function with a list of arguments.
   * Examples:
   * {{{
   *   - timeshift(avg:system.mem.free,100)
   *   - sum(avg:system.mem.free,avg:system.mem.used)
   * }}}
   *
   * @param fName   function name.
   * @param queries arguments.
   */
  final case class FuncQuery(fName: String, queries: MetricQuery*) extends MetricQuery {
    val q: String = fName + "(" + queries.map(_.q).mkString(",") + ")"
  }

  /**
   * Free-text query.
   *
   * @param text query text.
   */
  final case class FreeformQuery(text: String) extends MetricQuery {
    val q: String = text.trim
  }

  /**
   * Structured query builder that builds a query from components.
   *
   * @param metric     metric name.
   * @param aggregator metric aggregator.
   * @param scope      query scope.
   * @param groupBy    groping criteria.
   */
  final case class QueryBuilder(metric: String,
                                aggregator: MetricAggregator = MetricAggregator.Avg,
                                scope: Scope = Scope.All,
                                groupBy: Seq[TagName] = Nil) extends MetricQuery {

    def aggregate(aggregator: MetricAggregator) = copy(aggregator = aggregator)

    def filterBy(elements: ScopeElement*) = copy(scope = Scope.Filter(elements: _*))

    def groupBy(items: String*) = copy(groupBy = groupBy ++ items.map(TagName.apply))

    private val groupClause = if (groupBy.isEmpty) "" else "by" + groupBy.mkString("{", ",", "}")

    val q: String = s"${aggregator.entryName}:$metric{$scope}$groupClause"
  }

  /**
   * JSON serializer for metric queries.
   */
  val serializer: CustomSerializer[MetricQuery] = new CustomSerializer[MetricQuery](_ => ( {
    case JString(str) => FreeformQuery(str)
  }, {
    case query: MetricQuery => JString(query.q)
  }))
}