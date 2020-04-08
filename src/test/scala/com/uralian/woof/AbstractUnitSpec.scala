package com.uralian.woof

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

/**
 * Base trait for unit test specifications.
 */
abstract class AbstractUnitSpec extends Suite
  with WordSpecLike
  with MustMatchers
  with BeforeAndAfterAll
  with OptionValues
  with ScalaFutures
  with Inside {

  /**
   * Default timeout for Future testing.
   */
  implicit val defaultPatience = PatienceConfig(timeout = Span(10, Seconds), interval = Span(250, Millis))

  /**
   * @return the current time truncated to seconds as a java Instant.
   */
  def currentTime() = java.time.Instant.now.truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
}