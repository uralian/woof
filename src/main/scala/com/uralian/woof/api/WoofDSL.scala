package com.uralian.woof.api

import com.uralian.woof.api.MetricQuery.FreeformQuery

/**
 * DSL constructs.
 */
trait WoofDSL {

  /**
   * Implicitly converts a tuple (String, String) into a DataDog Tag.
   *
   * @param pair name->value pair.
   * @return a new Tag.
   */
  implicit def pairToTag(pair: (String, String)): Tag = Tag(pair._1, pair._2)

  /**
   * Implicitly converts a string into a ScopeElement:
   * {{{
   * "key:value" -> Tag(key, value)
   * "name" -> TagName(name)
   * "$name" -> VarName(name)
   * }}}
   *
   * @param str
   * @return a new scope element.
   */
  implicit def stringToScopeElement(str: String): ScopeElement = ScopeElement(str)

  /**
   * Implicitly converts a string into a freeform query.
   *
   * @param str query text.
   * @return a new freeform metric query.
   */
  implicit def stringToMetricQuery(str: String): FreeformQuery = MetricQuery.direct(str)

  /**
   * Implicitly converts a metric query into a metric query w/o alias construct.
   *
   * @param query metric query.
   * @return a pair MetricQuery->None.
   */
  implicit def queryToQueryAlias(query: MetricQuery): QueryWithAlias = query -> None
}

/**
 * DSL object.
 */
object dsl extends WoofDSL