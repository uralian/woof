package com.uralian.woof.util

import scala.concurrent._
import scala.concurrent.duration.{Duration, DurationLong}

/**
 * Provides a method for retrying a block of code multiple times until it succeeds or the number of unsuccessful
 * attempts exceeds the maximum allowed limit.
 */
object Retry {

  /**
   * exponential back off for retry
   */
  def exponentialBackoff(r: Int): Duration = scala.math.pow(2, scala.math.min(r, 8)).round * 100 milliseconds

  /**
   * Does not ignore any exception.
   */
  def noIgnore(t: Throwable): Boolean = false

  /**
   * Retries a particular block that can fail
   *
   * @param maxRetry        how many times to retry before to giveup
   * @param backoff         a back-off function that returns a Duration after which to retry.
   *                        default is an exponential backoff at 100 milliseconds steps
   * @param ignoreThrowable if you want to stop retrying on a particular exception
   * @param block           a block of code to retry
   * @param ctx             an execution context where to execute the block
   * @return an eventual Future succeeded with the value computed or failed with one of:
   *         `TooManyRetriesException`	if there were too many retries without an exception being caught.
   *         Probably impossible if you pass decent parameters
   *         `TimeoutException`	if you provide a deadline and the block takes too long to execute
   *         `Throwable`	the last encountered exception
   */
  def retry[T](maxRetry: Int,
               backoff: Int => Duration = exponentialBackoff,
               ignoreThrowable: Throwable => Boolean = noIgnore)(block: => T)
              (implicit ctx: ExecutionContext): Future[T] = {

    class TooManyRetriesException extends Exception("too many retries without exception")

    val p = Promise[T]()

    def recursiveRetry(retryCnt: Int, exception: Option[Throwable])(f: () => T): Option[T] = {
      if (maxRetry == retryCnt) {
        exception match {
          case Some(t) => p failure t
          case None    => p failure new TooManyRetriesException
        }
        None
      } else {
        val success = try {
          Some(f())
        } catch {
          case t: Throwable if !ignoreThrowable(t) =>
            blocking {
              val interval = backoff(retryCnt).toMillis
              Thread.sleep(interval)
            }
            recursiveRetry(retryCnt + 1, Some(t))(f)
          case t: Throwable                        =>
            p failure t
            None
        }
        success map { v =>
          p success v
          v
        }
      }
    }

    def doBlock() = block

    Future {
      recursiveRetry(0, None)(() => doBlock())
    }

    p.future
  }

  /**
   * Retries a code returning a future.
   *
   * @param maxRetry
   * @param block
   * @param ctx
   * @tparam T
   * @return
   */
  def retryFuture[T](maxRetry: Int, delay: Duration = Duration.Zero)
                    (block: () => Future[T])(implicit ctx: ExecutionContext): Future[T] = {

    def recursiveRetry(retryCnt: Int, f: () => Future[T]): Future[T] = if (retryCnt >= maxRetry)
      f()
    else
      f().recoverWith {
        case _ => blocking(Thread.sleep(delay.toMillis)); recursiveRetry(retryCnt + 1, f)
      }

    recursiveRetry(1, block)
  }
}