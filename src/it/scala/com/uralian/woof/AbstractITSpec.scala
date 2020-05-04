package com.uralian.woof

import com.typesafe.config.ConfigFactory
import com.uralian.woof.api.Tag
import org.json4s.JValue
import org.json4s.native.JsonMethods.{compact, pretty, render}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

import scala.collection.JavaConverters._
import scala.util.Random

/**
 * Base trait for integration test specifications.
 */
abstract class AbstractITSpec extends Suite
  with WordSpecLike
  with MustMatchers
  with BeforeAndAfterAll
  with OptionValues
  with ScalaFutures
  with Inside {

  /* test configuration */
  val config = ConfigFactory.load("integration.conf")
  val host = config.getString("host.name")
  val defaultTags = config.getStringList("host.tags").asScala.map(Tag.apply).toSet

  /**
   * Default timeout for Future testing.
   */
  implicit val defaultPatience = PatienceConfig(timeout = Span(60, Seconds), interval = Span(250, Millis))

  /**
   * @return the current time truncated to seconds as a java Instant.
   */
  def currentTime() = java.time.Instant.now.truncatedTo(java.time.temporal.ChronoUnit.SECONDS)

  /**
   * Generates a random string of alphanumeric characters.
   *
   * @param size string size.
   * @return a new string of a given size consisting of random alphanumeric characters.
   */
  def randomString(size: Int = 10) = Random.alphanumeric.take(size).mkString

  /**
   * Renders a JSON document.
   *
   * @param json   json to render.
   * @param format if `true`, it will be pretty-formatted.
   * @return a formatted json string.
   */
  def renderJson(json: JValue, format: Boolean = true) = if (format) pretty(render(json)) else compact(render(json))
}
