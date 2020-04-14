package com.uralian.woof.util

import java.net.URL
import java.time.Instant

import org.json4s._

import scala.concurrent.duration._

/**
 * JSON helper methods.
 */
trait JsonUtils {

  type FSer = PartialFunction[(String, Any), Option[(String, Any)]]
  type FDes = PartialFunction[JField, JField]

  /**
   * Combines a list of partial functions of the same type via `orElse` method.
   *
   * @param pfs partial functions of the same signature [A, B].
   * @tparam A argument type.
   * @tparam B result type.
   * @return a union of the supplied partial functions.
   */
  def combine[A, B](pfs: PartialFunction[A, B]*) = pfs.tail.foldLeft(pfs.head) { (acc, pf) =>
    acc orElse pf
  }

  /**
   * Combines partial functions for individual fields to be renamed.
   *
   * @param pairs collection of scalaName->jsonName mappings.
   * @return a function doing class field name translation before converting them to JSON.
   */
  def renameFieldsToJson(pairs: (String, String)*): FSer = combine(pairs.map {
    case (scalaName, jsonName) => FieldSerializer.renameTo(scalaName, jsonName)
  }: _*)

  /**
   * Combines partial functions for individual fields to be renamed.
   *
   * @param pairs collection of jsonName->scalaName mappings.
   * @return a function doing class field name translation after restoring them from JSON.
   */
  def renameFieldsFromJson(pairs: (String, String)*): FDes = combine(pairs.map {
    case (jsonName, scalaName) => FieldSerializer.renameFrom(jsonName, scalaName)
  }: _*)

  /**
   * Builds a field serializer for translating class field names between Scala and JSON.
   *
   * @param pairs collection of scalaName->jsonName mappings.
   * @tparam T class type.
   * @return the field serializer.
   */
  def translateFields[T: Manifest](pairs: (String, String)*) = FieldSerializer[T](
    serializer = renameFieldsToJson(pairs: _*),
    deserializer = renameFieldsFromJson(pairs.map(p => p._2 -> p._1): _*)
  )

  /* common serializers */

  val instantSerializerAsSeconds = new CustomSerializer[Instant](_ => ( {
    case JLong(num) => Instant.ofEpochSecond(num)
    case JInt(num)  => Instant.ofEpochSecond(num.toLong)
  }, {
    case t: Instant => JLong(t.getEpochSecond)
  }))

  val instantSerializerAsMillis = new CustomSerializer[Instant](_ => ( {
    case JLong(num) => Instant.ofEpochMilli(num)
    case JInt(num)  => Instant.ofEpochMilli(num.toLong)
  }, {
    case t: Instant => JLong(t.toEpochMilli)
  }))

  val durationSerializerAsSeconds = new CustomSerializer[Duration](_ => ( {
    case JLong(num) => num seconds
    case JInt(num)  => num.toLong seconds
  }, {
    case d: Duration => JLong(d.toSeconds)
  }))

  val urlSerializer = new CustomSerializer[URL](_ => ( {
    case JString(str) => new URL(str)
  }, {
    case url: URL => JString(url.toString)
  }))
}

/**
 * A singleton for JSON helper methods.
 */
object JsonUtils extends JsonUtils {
  /**
   * Include serializers for common types; java.time.Instant is serialized as seconds.
   */
  val commonSerializers = List(instantSerializerAsSeconds, durationSerializerAsSeconds, urlSerializer)
}
