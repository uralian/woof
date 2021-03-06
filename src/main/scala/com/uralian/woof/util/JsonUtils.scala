package com.uralian.woof.util

import java.net.URL
import java.time._
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

import enumeratum.EnumEntry
import org.json4s._

/**
 * JSON helper methods.
 */
trait JsonUtils {

  /* implicits */
  implicit def enum2jvalue(x: EnumEntry) = JString(x.entryName)

  /* for custom serializers */
  type FromJson[T] = PartialFunction[JValue, T]
  type ToJson = PartialFunction[Any, JValue]

  /**
   * Default deserializer for a given type.
   *
   * @param fmt
   * @tparam T
   * @return
   */
  def baseFromJson[T](fmt: Formats, mf: Manifest[T]): FromJson[T] = {
    case jv => jv.extract[T](fmt, mf)
  }

  /**
   * Default serializer.
   *
   * @param fmt
   * @return
   */
  def baseToJson(fmt: Formats): ToJson = {
    case x => Extraction.decompose(x)(fmt)
  }

  /**
   * Creates a custom serializer from from/to JSON functions.
   *
   * @param fromJson
   * @param toJson
   * @tparam T
   * @return
   */
  def customSerializer[T: Manifest](fromJson: (Formats, Manifest[T]) => FromJson[T] = baseFromJson[T] _,
                                    toJson: Formats => ToJson = baseToJson _) =
    new CustomSerializer[T](fmt => (fromJson(fmt, implicitly), toJson(fmt)))

  /* for field serializers */
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
  def combine[A, B](pfs: PartialFunction[A, B]*): PartialFunction[A, B] = if (pfs.isEmpty)
    PartialFunction.empty[A, B]
  else
    pfs.tail.foldLeft(pfs.head) { (acc, pf) => acc orElse pf }

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
   * Combines partial functions for individual fields to be ignored.
   *
   * @param names field names to ignore.
   * @return a function ignoring any of the supplied field names.
   */
  def ignoreFields(names: String*): FSer = combine(names.map(name => FieldSerializer.ignore(name)): _*)

  /**
   * Builds a field serializer for translating class field names between Scala and JSON.
   *
   * @param pairs collection of scalaName->jsonName mappings. If jsonName is null, the field is dropped.
   * @tparam T class type.
   * @return the field serializer.
   */
  def translateFields[T: Manifest](pairs: (String, String)*): FieldSerializer[T] = {
    val toRename = pairs filterNot (_._2 == null)
    val toIgnore = pairs collect { case (name, null) => name }
    FieldSerializer[T](
      serializer = combine(renameFieldsToJson(toRename: _*), ignoreFields(toIgnore: _*)),
      deserializer = renameFieldsFromJson(toRename.map(p => p._2 -> p._1): _*)
    )
  }

  /* common serializers */

  private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS[XXX][X]")

  private def parseInstant(str: String): Instant = fmt.parse(str, (t: TemporalAccessor) => Instant.from(t))

  val instantSerializerAsSeconds = new CustomSerializer[Instant](_ => ( {
    case JLong(num)   => Instant.ofEpochSecond(num)
    case JInt(num)    => Instant.ofEpochSecond(num.toLong)
    case JString(str) => parseInstant(str)
  }, {
    case t: Instant => JLong(t.getEpochSecond)
  }))

  val instantSerializerAsMillis = new CustomSerializer[Instant](_ => ( {
    case JLong(num)   => Instant.ofEpochMilli(num)
    case JInt(num)    => Instant.ofEpochMilli(num.toLong)
    case JString(str) => parseInstant(str)
  }, {
    case t: Instant => JLong(t.toEpochMilli)
  }))

  val durationSerializerAsSeconds = new CustomSerializer[Duration](_ => ( {
    case JLong(num) => Duration.ofSeconds(num)
    case JInt(num)  => Duration.ofSeconds(num.toLong)
  }, {
    case d: Duration => JLong(d.getSeconds)
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