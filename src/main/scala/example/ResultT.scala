package example

import scala.concurrent.ExecutionContext.parasitic
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure
import scala.util.control.NonFatal

trait ResultT[+E <: Throwable, +A] {
  self =>
  def toFuture: Future[A]

  def map[B](f: A => B)(implicit ec: ExecutionContext): Result[E, B] =
    self.flatMap(a => Result.succeed(f(a)))(ec)

  def flatMap[E2 >: E <: Throwable, B](f: A => Result[E2, B])(implicit ec: ExecutionContext): Result[E2, B] =
    Result[E2, B] {
      for {
        a <- self.toFuture
        b <- f(a).toFuture
      } yield b
    }

  def flatten[E2 >: E <: Throwable, B](implicit ev: A <:< Result[E2, B]): Result[E2, B] =
    flatMap(ev)(parasitic)

  def mapError[E2 <: Throwable](f: E => E2)(implicit ec: ExecutionContext): Result[E2, A] =
    Result[E2, A] {
      self.toFuture.transform {
        case Failure(e: E) => Failure(f(e))
        case success       => success
      }
    }

  def zip[E2 >: E <: Throwable, B](that: Result[E2, B]): Result[E2, (A, B)] =
    zipWith(that)(Result.zipWithTuple2Fun)(parasitic)

  def zipWith[E2 >: E <: Throwable, U, R](that: Result[E2, U])(f: (A, U) => R)(implicit
    ec: ExecutionContext
  ): Result[E2, R] =
    Result(toFuture.zipWith(that.toFuture)(f))

  def catchAll[E2 >: E <: Throwable, A2 >: A](f: E => Result[E2, A2])(implicit
    ec: ExecutionContext
  ): Result[E2, A2] =
    Result[E2, A2] {
      self.toFuture.transformWith {
        case Failure(e: E) if NonFatal(e) => f(e).toFuture
        case _                            => self.toFuture
      }
    }

  def catchSome[E2 >: E <: Throwable, A2 >: A](pf: PartialFunction[E, Result[E2, A2]])(implicit
    ec: ExecutionContext
  ): Result[E2, A2] =
    Result[E2, A2] {
      self.toFuture.transformWith {
        case Failure(e: E) if NonFatal(e) && pf.isDefinedAt(e) => pf(e).toFuture
        case _                                                 => self.toFuture
      }
    }
}
