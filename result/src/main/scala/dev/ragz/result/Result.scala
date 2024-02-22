package dev.ragz.result

import dev.ragz.result.FutureFailedException.FutureFailedException

import scala.concurrent.ExecutionContext.parasitic
import scala.concurrent.duration.Duration
import scala.concurrent.{Awaitable, CanAwait, ExecutionContext, Future => StdFuture}
import scala.util.control.NoStackTrace
import scala.util.{Failure, Success, Try}

sealed trait Result[+E <: Throwable, +A] extends Awaitable[A] {
  self =>
  def toFuture: StdFuture[A]

  def value: Option[Try[A]] = self.toFuture.value

  def map[B](f: A => B)(implicit ec: ExecutionContext): Result[E, B] =
    Result(self.toFuture.transform(_ map f))

  def flatMap[E2 >: E <: Throwable, B](f: A => Result[E2, B])(implicit ec: ExecutionContext): Result[E2, B] =
    Result(self.toFuture.flatMap(f(_).toFuture))

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
    Result(self.toFuture.zipWith(that.toFuture)(f))

  def catchAll[E2 >: E <: Throwable, A2 >: A](f: E => Result[E2, A2])(implicit
                                                                      ec: ExecutionContext
  ): Result[E2, A2] =
    Result[E2, A2] {
      self.toFuture.transformWith {
        case Failure(e) if NonFatal(e) => f(e.asInstanceOf[E]).toFuture
        case _                         => self.toFuture
      }
    }

  def catchSome[E2 >: E <: Throwable, A2 >: A](pf: PartialFunction[E, Result[E2, A2]])(implicit
                                                                                       ec: ExecutionContext
  ): Result[E2, A2] =
    Result[E2, A2] {
      self.toFuture.transformWith {
        case Failure(e) if NonFatal(e) && pf.isDefinedAt(e.asInstanceOf[E]) =>
          pf(e.asInstanceOf[E]).toFuture
        case _                                                              =>
          self.toFuture
      }
    }
  private final val failedFailure                                           =
    Failure[Nothing](
      new NoSuchElementException("Future.failed not completed with error E.") with NoStackTrace
    )

  private final def failedFun[B](v: Try[B]): Try[E] =
    v match {
      case Failure(e) if NonFatal(e) => Success(e.asInstanceOf[E])
      case Failure(exception)        => Failure(exception)
      case Success(_)                => failedFailure
    }

  def failed: Result[FutureFailedException, E] =
    transform(failedFun)(parasitic).asInstanceOf[Result[FutureFailedException, E]]

  def foreach[U](f: A => U)(implicit executor: ExecutionContext): Unit =
    onComplete(_ foreach f)

  def onComplete[B](f: Try[A] => B)(implicit executor: ExecutionContext): Unit =
    self.toFuture.onComplete(f)

  def isCompleted: Boolean = self.toFuture.isCompleted

  def transform[B](f: Try[A] => Try[B])(implicit executor: ExecutionContext): Result[Throwable, B] =
    Result(self.toFuture.transform(f))

  def transformWith[E2 >: E <: Throwable, B](f: Try[A] => Result[E2, B])(implicit
                                                                         executor: ExecutionContext
  ): Result[E2, B] =
    Result(self.toFuture.transformWith(f(_).toFuture))

//  @throws(classOf[TimeoutException])
//  @throws(classOf[InterruptedException])
  def ready(atMost: Duration)(implicit permit: CanAwait): this.type = {
    self.toFuture.ready(atMost)
    this
  }

//  @throws(classOf[TimeoutException])
//  @throws(classOf[InterruptedException])
  def result(atMost: Duration)(implicit permit: CanAwait): A =
    self.toFuture.result(atMost)
}

object Result {

  private final case class Attempt[+E <: Throwable, +A](future: StdFuture[A]) extends Result[E, A] {
    override def toFuture: StdFuture[A] = future
  }

  private final case class Success[+A](success: A) extends Result[Nothing, A] {
    override def toFuture: StdFuture[A] = StdFuture.successful(success)
  }

  private final case class Failed[+E <: Exception](failure: E) extends Result[E, Nothing] {
    override def toFuture: StdFuture[Nothing] = StdFuture.failed(failure)
  }

  private final case class Fatal(failure: Throwable) extends Result[FatalError, Nothing] {
    override def toFuture: StdFuture[Nothing] = StdFuture.failed(FatalError(failure))
  }

  private final val _zipWithTuple2: (Any, Any) => (Any, Any) = Tuple2.apply

  private[result] final def zipWithTuple2Fun[T, U]: (T, U) => (T, U) =
    _zipWithTuple2.asInstanceOf[(T, U) => (T, U)]

  def unapply[E <: Throwable, A](result: Result[E, A]): Option[StdFuture[A]] =
    Some(result.toFuture)

  private final def apply[E <: Throwable, A](future: StdFuture[A]): Result[E, A] =
    Attempt[E, A](future)

  final def apply[A](body: => A)(implicit ec: ExecutionContext): Result[Throwable, A] =
    Result[Throwable, A](StdFuture(body))

  final def fromFuture[A](future: StdFuture[A]): Result[Throwable, A] =
    Result(future)

  final def fromEither[E <: Throwable, A](either: Either[E, A]): Result[E, A] =
    Result(StdFuture.fromTry(either.toTry))

  final def fromTry[A](body: Try[A]): Result[Throwable, A] =
    Result[Throwable, A](StdFuture.fromTry(body))

  final def successful[A](value: A): Result[Nothing, A] =
    Success[A](value)

  final def failed[E <: Exception](exception: E): Result[E, Nothing] =
    Failed[E](exception)

  final def fatal(exception: Throwable): Result[FatalError, Nothing] =
    Fatal(exception)

  final def sequence[E <: Throwable, A](results: Seq[Result[E, A]])(implicit
                                                                    ec: ExecutionContext
  ): Result[E, Seq[A]] =
    Result(StdFuture.sequence(results.map(_.toFuture)))

}
