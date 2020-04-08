package com.uralian.woof

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

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
}
