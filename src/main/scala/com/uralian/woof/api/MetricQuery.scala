package com.uralian.woof.api

import com.uralian.woof.api.MetricQuery.CompoundQuery
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
  def +(q2: MetricQuery): CompoundQuery = combine("+")(q2)

  /**
   * Combines this query with another one via "-".
   *
   * @param q2 second query.
   * @return compound query that evaluates to string this.r-q2.r
   */
  def -(q2: MetricQuery) = combine("-")(q2)

  /**
   * Combines this query with another one via "*".
   *
   * @param q2 second query.
   * @return compound query that evaluates to string this.r*q2.r
   */
  def *(q2: MetricQuery) = combine("*")(q2)

  /**
   * Combines this query with another one via "/".
   *
   * @param q2 second query.
   * @return compound query that evaluates to string this.r/q2.r
   */
  def /(q2: MetricQuery) = combine("/")(q2)

  /**
   * Combines this query with another one via an arbitrary separator
   *
   * @param separator separator string.
   * @param q2        second query.
   * @return compound query that evaluates to string this.r<separator>q2.r
   */
  def combine(separator: String)(q2: MetricQuery) = CompoundQuery(this, separator, q2)
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
  def text(str: String) = FreeformQuery(str)

  /**
   * Creates a new query builder based on the supplied metric.
   *
   * @param name metric name.
   * @return a new query builder.
   */
  def metric(name: String) = QueryBuilder(metric = name)

  /**
   * Combines two queries together via a separator, which can be "*", "/", etc.
   *
   * @param q1
   * @param separator
   * @param q2
   */
  final case class CompoundQuery(q1: MetricQuery, separator: String, q2: MetricQuery) extends MetricQuery {
    val q: String = s"${q1.q}$separator${q2.q}"
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
   * @param func       transformation to apply to the rendered query, which allows furter customization to build queries
   *                   like {{{function1(avg:system.cpu.user{env:qa}by{host}, arg1, arg2, ...)}}}
   */
  final case class QueryBuilder(metric: String,
                                aggregator: MetricAggregator = MetricAggregator.Avg,
                                scope: Scope = Scope.All,
                                groupBy: Seq[TagName] = Nil,
                                func: String => String = identity[String]) extends MetricQuery {

    def aggregate(aggregator: MetricAggregator) = copy(aggregator = aggregator)

    def filterBy(elements: ScopeElement*) = copy(scope = Scope.Filter(elements: _*))

    def groupBy(items: String*) = copy(groupBy = groupBy ++ items.map(TagName.apply))

    def transform(f: String => String) = copy(func = func andThen f)

    def wrapIn(fName: String, args: Any*) =
      transform(q => fName + "(" + q + args.map(x => "," + x).mkString + ")")

    private val groupClause = if (groupBy.isEmpty) "" else "by" + groupBy.mkString("{", ",", "}")

    val q: String = func(s"${aggregator.entryName}:$metric{$scope}$groupClause")
  }

  /**
   * JSON serializer for metric queries.
   */
  val serializer: CustomSerializer[MetricQuery] = new CustomSerializer[MetricQuery](_ => ( {
    case JString(str) => text(str)
  }, {
    case query: MetricQuery => JString(query.q)
  }))
}