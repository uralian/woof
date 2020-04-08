package com.uralian.woof

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

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
  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(250, Millis))
}
