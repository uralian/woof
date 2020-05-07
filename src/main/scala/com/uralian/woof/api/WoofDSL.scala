package com.uralian.woof.api

import java.time.Instant

import com.uralian.woof.api.MetricQuery.FreeformQuery
import com.uralian.woof.api.graphs.{FormatColor, FormatPalette}
import com.uralian.woof.api.metrics.Point

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

  /**
   * Adds functionality to FormatColor.
   *
   * @param underlying format color.
   */
  implicit class RichFormatColor(val underlying: FormatColor) {
    /**
     * Creates a standard format palette using current color for text and the argument for background.
     *
     * @param bgColor background color.
     * @return standard format palette.
     */
    def on(bgColor: FormatColor): FormatPalette.Standard = FormatPalette.Standard(underlying, bgColor)
  }

  /**
   * Implicitly converts a tuple (Instant, Double) into a data Point.
   *
   * @param pair a tuple (time, value).
   * @return a new data point.
   */
  implicit def pairToPoint(pair: (Instant, Double)): Point = Point(pair._1, BigDecimal(pair._2))
}

/**
 * DSL object.
 */
object dsl extends WoofDSL