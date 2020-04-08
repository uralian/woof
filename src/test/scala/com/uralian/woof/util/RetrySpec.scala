package com.uralian.woof.util

import com.uralian.woof.AbstractUnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
 * Retry test suite.
 */
class RetrySpec extends AbstractUnitSpec {

  import RetrySpec._

  def noBackoff(r: Int): Duration = Duration.Zero

  "Retry.retry" should {
    "fail once retry limit is reached" in {
      val block = new TestBlock()
      val result = Retry.retry(3, noBackoff)(block.run(20))
      result.failed.futureValue mustBe TestBlockException(3, 20)
    }
    "handle the success case" in {
      val block = new TestBlock()
      val result = Retry.retry(2, Retry.exponentialBackoff)(block.run(2))
      result.futureValue mustBe "OO"
    }
  }

  "Retry.retryFuture" should {
    "return Exception from code block once retry limit is reached" in {
      val block = new TestBlock
      val result = Retry.retryFuture(3)(() => block.runAsync(20))
      result.failed.futureValue mustBe TestBlockException(3, 20)
    }
    "handle the success case when running a Future" in {
      val block = new TestBlock()
      val result = Retry.retryFuture(2)(() => block.runAsync(2))
      result.futureValue mustBe "OO"

    }
  }
}

/**
 * Provides test data for RetrySpec suite.
 */
object RetrySpec {

  case class TestBlockException(size: Int, cutoff: Int) extends Exception(s"Too short $size/$cutoff")

  class TestBlock {
    @volatile private var buffer: String = ""

    def run(cutoff: Int): String = {
      buffer = buffer + "O"
      if (buffer.length < cutoff)
        throw TestBlockException(buffer.length, cutoff)
      else
        buffer
    }

    def runAsync(cutoff: Int): Future[String] = Future(run(cutoff))
  }
}