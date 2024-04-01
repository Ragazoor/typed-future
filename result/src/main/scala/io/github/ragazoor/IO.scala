package io.github.ragazoor

import IOFailedException.IOFailedException

import scala.concurrent.ExecutionContext.parasitic
import scala.concurrent.duration.Duration
import scala.concurrent.{ Awaitable, CanAwait, ExecutionContext, Future => StdFuture }
import scala.util.control.NoStackTrace
import scala.util.{ Failure, Success, Try }

sealed trait IO[+E <: Throwable, +A] extends Awaitable[A] {
  self =>
  def toFuture: StdFuture[A]

  def value: Option[Try[A]] = self.toFuture.value

  def map[B](f: A => B)(implicit ec: ExecutionContext): IO[E, B] =
    IO(self.toFuture.transform(_ map f))

  def mapTry[B](f: A => Try[B])(implicit ec: ExecutionContext): IO[Throwable, B] =
    IO(self.toFuture.transform(_ flatMap f))

  def mapEither[E2 >: E <: Throwable, B](f: A => Either[E2, B])(implicit ec: ExecutionContext): IO[E2, B] =
    IO(self.toFuture.transform(_ flatMap (f(_).toTry)))

  def flatMap[E2 >: E <: Throwable, B](f: A => IO[E2, B])(implicit ec: ExecutionContext): IO[E2, B] =
    IO(self.toFuture.flatMap(f(_).toFuture))

  def flatten[E2 >: E <: Throwable, B](implicit ev: A <:< IO[E2, B]): IO[E2, B] =
    flatMap(ev)(parasitic)

  def mapError[E2 <: Throwable](f: E => E2)(implicit ec: ExecutionContext): IO[E2, A] =
    IO[E2, A] {
      self.toFuture.transform {
        case Failure(e) => Failure(f(e.asInstanceOf[E]))
        case success    => success
      }
    }

  def zip[E2 >: E <: Throwable, B](that: IO[E2, B]): IO[E2, (A, B)] =
    zipWith(that)(IO.zipWithTuple2Fun)(parasitic)

  def zipWith[E2 >: E <: Throwable, U, R](that: IO[E2, U])(f: (A, U) => R)(implicit
    ec: ExecutionContext
  ): IO[E2, R] =
    IO(self.toFuture.zipWith(that.toFuture)(f))

  def catchAll[E2 >: E <: Throwable, A2 >: A](f: E => IO[E2, A2])(implicit
    ec: ExecutionContext
  ): IO[E2, A2] =
    IO[E2, A2] {
      self.toFuture.transformWith {
        case Failure(e) if NonFatal(e) => f(e.asInstanceOf[E]).toFuture
        case _                         => self.toFuture
      }
    }

  def catchSome[E2 >: E <: Throwable, A2 >: A](pf: PartialFunction[E, IO[E2, A2]])(implicit
    ec: ExecutionContext
  ): IO[E2, A2] =
    IO[E2, A2] {
      self.toFuture.transformWith {
        case Failure(e) if NonFatal(e) && pf.isDefinedAt(e.asInstanceOf[E]) =>
          pf(e.asInstanceOf[E]).toFuture
        case _                                                              =>
          self.toFuture
      }
    }
  private final val failedFailure                                   =
    Failure[Nothing](
      new NoSuchElementException("Future.failed not completed with error E.") with NoStackTrace
    )

  private final def failedFun[B](v: Try[B]): Try[E] =
    v match {
      case Failure(e) if NonFatal(e) => Success(e.asInstanceOf[E])
      case Failure(exception)        => Failure(exception)
      case Success(_)                => failedFailure
    }

  def failed: IO[IOFailedException, E] =
    transform(failedFun)(parasitic).asInstanceOf[IO[IOFailedException, E]]

  def foreach[U](f: A => U)(implicit executor: ExecutionContext): Unit =
    onComplete(_ foreach f)

  def onComplete[B](f: Try[A] => B)(implicit executor: ExecutionContext): Unit =
    self.toFuture.onComplete(f)

  def isCompleted: Boolean = self.toFuture.isCompleted

  def transform[B](f: Try[A] => Try[B])(implicit executor: ExecutionContext): IO[Throwable, B] =
    IO(self.toFuture.transform(f))

  def transformWith[E2 >: E <: Throwable, B](f: Try[A] => IO[E2, B])(implicit
    executor: ExecutionContext
  ): IO[E2, B] =
    IO(self.toFuture.transformWith(f(_).toFuture))

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

object IO {

  private[io] final case class Attempt[+E <: Throwable, +A](future: StdFuture[A]) extends IO[E, A] {
    override def toFuture: StdFuture[A] = future
  }

  private[io] final case class Successful[+A](success: A) extends IO[Nothing, A] {
    override def toFuture: StdFuture[A] = StdFuture.successful(success)
  }

  private[io] final case class Failure[+E <: Exception](failure: E) extends IO[E, Nothing] {
    override def toFuture: StdFuture[Nothing] = StdFuture.failed(failure)
  }

  private[io] final case class Fatal(failure: Throwable) extends IO[FatalError, Nothing] {
    override def toFuture: StdFuture[Nothing] = StdFuture.failed(FatalError(failure))
  }

  private final val _zipWithTuple2: (Any, Any) => (Any, Any) = Tuple2.apply

  private[io] final def zipWithTuple2Fun[T, U]: (T, U) => (T, U) =
    _zipWithTuple2.asInstanceOf[(T, U) => (T, U)]

  def unapply[E <: Throwable, A](result: IO[E, A]): Option[StdFuture[A]] =
    Some(result.toFuture)

  private[io] final def apply[E <: Throwable, A](future: StdFuture[A]): IO[E, A] =
    new IO[E, A] {
      override def toFuture: StdFuture[A] = future
    }

  final def apply[A](body: => A)(implicit ec: ExecutionContext): IO[Throwable, A] =
    IO[Throwable, A](StdFuture(body))

  final def fromFuture[A](future: StdFuture[A]): IO[Throwable, A] =
    IO(future)

  final def fromEither[E <: Throwable, A](either: Either[E, A]): IO[E, A] =
    IO(StdFuture.fromTry(either.toTry))

  final def fromTry[A](body: Try[A]): IO[Throwable, A] =
    IO[Throwable, A](StdFuture.fromTry(body))

  final def successful[A](value: A): IO[Nothing, A] =
    Successful[A](value)

  final def failed[E <: Exception](exception: E): IO[E, Nothing] =
    Failure[E](exception)

  final def fatal(exception: Throwable): IO[FatalError, Nothing] =
    Fatal(exception)

  final def sequence[E <: Throwable, A](results: Seq[IO[E, A]])(implicit
    ec: ExecutionContext
  ): IO[E, Seq[A]] =
    IO(StdFuture.sequence(results.map(_.toFuture)))

  val unit = IO.successful(())
}
