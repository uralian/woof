package com.uralian.woof

import com.uralian.woof.util.JsonUtils
import org.json4s.DefaultFormats

/**
 * Helper methods and types for API.
 */
package object api {

  type QueryWithAlias = (MetricQuery, Option[String])

  /**
   * JSON formats.
   */
  val apiFormats = DefaultFormats.withBigDecimal ++ JsonUtils.commonSerializers +
    ScopeElement.serializer + Tag.serializer + TagName.serializer + VarName.serializer + Scope.serializer +
    MetricQuery.serializer
}
