package dev.ragz.result

import scala.concurrent.ExecutionContext.parasitic
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait Result[+E <: Throwable, +A] {
  self =>
  def toFuture: Future[A]

  def value: Option[Try[A]] = self.toFuture.value

  def map[B](f: A => B)(implicit ec: ExecutionContext): Result[E, B] =
    Result.succeed(self.toFuture.transform(_ map f))

  def flatMap[E2 >: E <: Throwable, B](f: A => Result[E2, B])(implicit ec: ExecutionContext): Result[E2, B] = {
    Result.fromFuture(self.toFuture.transformWith {
        t =>
          if (t.isInstanceOf[Success[A]]) f(t.asInstanceOf[Success[A]].value)
          else this.asInstanceOf[Success[B]] // Safe cast
      }
    )
    Result[E2, B] {
      for {
        a <- self.toFuture
        b <- f(a).toFuture
      } yield b
    }
  }

  def flatten[E2 >: E <: Throwable, B](implicit ev: A <:< Result[E2, B]): Result[E2, B] =
    flatMap(ev)(parasitic)

  def mapError[E2 <: Throwable](f: E => E2)(implicit ec: ExecutionContext): Result[E2, A] =
    Result[E2, A] {
      self.toFuture.transform {
        case Failure(e) => Failure(f(e.asInstanceOf[E]))
        case success    => success
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
        case Failure(e) if ResultNonFatal(e) => f(e.asInstanceOf[E]).toFuture
        case _                               => self.toFuture
      }
    }

  def catchSome[E2 >: E <: Throwable, A2 >: A](pf: PartialFunction[E, Result[E2, A2]])(implicit
    ec: ExecutionContext
  ): Result[E2, A2] =
    Result[E2, A2] {
      self.toFuture.transformWith {
        case Failure(e) if ResultNonFatal(e) && pf.isDefinedAt(e.asInstanceOf[E]) =>
          pf(e.asInstanceOf[E]).toFuture
        case _                                                                    =>
          self.toFuture
      }
    }
}

object Result {

  private final case class Attempt[+E <: Throwable, +A] private (future: Future[A]) extends Result[E, A] {
    override def toFuture: Future[A] = future
  }

  private final case class Success[+A] private (success: A) extends Result[Nothing, A] {
    override def toFuture: Future[A] = Future.successful(success)
  }

  private final case class Failed[+E <: Exception] private (failure: E) extends Result[E, Nothing] {
    override def toFuture: Future[Nothing] = Future.failed(failure)
  }

  private final case class Fatal private (failure: Throwable) extends Result[FatalError, Nothing] {
    override def toFuture: Future[Nothing] = Future.failed(FatalError(failure))
  }

  private final val _zipWithTuple2: (Any, Any) => (Any, Any) = Tuple2.apply _

  private[result] final def zipWithTuple2Fun[T, U]: (T, U) => (T, U) =
    _zipWithTuple2.asInstanceOf[(T, U) => (T, U)]

  def unapply[E <: Throwable, A](result: Result[E, A]): Option[Future[A]] =
    Some(result.toFuture)

  private def apply[E <: Throwable, A](future: Future[A]): Result[E, A] =
    Attempt(future)

  final def apply[A](body: => A)(implicit ec: ExecutionContext): Result[Throwable, A] =
    Result[Throwable, A](Future(body))

  final def fromFuture[A](future: Future[A]): Result[Throwable, A] =
    Result(future)

  final def fromTry[A](body: Try[A]): Result[Throwable, A] =
    Result[Throwable, A](Future.fromTry(body))

  final def succeed[A](value: A): Result[Nothing, A] =
    Success(value)

  final def fail[E <: Exception](exception: E): Result[E, Nothing] =
    Failed(exception)

  final def fatal(exception: Throwable): Result[FatalError, Nothing] =
    Fatal(exception)

  final def sequence[E <: Throwable, A](results: Seq[Result[E, A]])(implicit
    ec: ExecutionContext
  ): Result[E, Seq[A]] =
    Attempt(Future.sequence(results.map(_.toFuture)))

}
