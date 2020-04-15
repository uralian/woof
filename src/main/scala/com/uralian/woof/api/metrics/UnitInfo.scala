package com.uralian.woof.api.metrics

import com.uralian.woof.util.JsonUtils._
import org.json4s.native.Serialization

/**
 * Metric unit information.
 *
 * @param family      unit family.
 * @param scaleFactor scaling factor.
 * @param name        unit name.
 * @param shortName   unit short name.
 * @param plural      plural form of unit name.
 * @param id          unit it.
 */
final case class UnitInfo(family: String,
                          scaleFactor: Double,
                          name: String,
                          shortName: String,
                          plural: String,
                          id: Int) {
  override def toString: String = Serialization.write(this)
}

/**
 * Provides JSON serializer for [[UnitInfo]].
 */
object UnitInfo {
  val serializer = translateFields[UnitInfo]("scaleFactor" -> "scale_factor", "shortName" -> "short_name")
}