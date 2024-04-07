package io.github.ragazoor

import io.github.ragazoor.AttemptUtils.{failedFailure, zipWithTuple2Fun}

import scala.concurrent.ExecutionContext.parasitic
import scala.concurrent.duration.Duration
import scala.concurrent.{Awaitable, CanAwait, ExecutionContext, TimeoutException, Future => StdFuture}
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

sealed trait Attempt[+E <: Throwable, +A] extends Awaitable[A] {
  self =>
  def toFuture: StdFuture[A]
  val isFatal: Boolean

  def value: Option[Try[A]] = self.toFuture.value

  def map[B](f: A => B)(implicit ec: ExecutionContext): Attempt[E, B] =
    Attempt(self.toFuture.transform(_ map f), isFatal)

  def mapTry[B](f: A => Try[B])(implicit ec: ExecutionContext): Attempt[Throwable, B] =
    Attempt(self.toFuture.transform(_ flatMap f), isFatal)

  def mapEither[E2 >: E <: Throwable, B](f: A => Either[E2, B])(implicit ec: ExecutionContext): Attempt[E2, B] =
    Attempt(self.toFuture.transform(_ flatMap (f(_).toTry)), isFatal)

  def flatMap[E2 >: E <: Throwable, B](f: A => Attempt[E2, B])(implicit ec: ExecutionContext): Attempt[E2, B] =
    Attempt(self.toFuture.flatMap(f(_).toFuture), isFatal)

  def flatten[E2 >: E <: Throwable, B](implicit ev: A <:< Attempt[E2, B]): Attempt[E2, B] =
    flatMap(ev)(parasitic)

  def mapError[E2 <: Throwable](f: E => E2)(implicit ec: ExecutionContext): Attempt[E2, A] = {
    var isFutureFatal     = isFatal
    val transformedFuture = self.toFuture.transform {
      case Failure(e) if NonFatal(e) && !isFatal => Failure(f(e.asInstanceOf[E]))
      case Failure(e) if !NonFatal(e) || isFatal =>
        isFutureFatal = true
        Failure(e)
      case success                               => success
    }
    Attempt[E2, A](transformedFuture, isFutureFatal)
  }

  def zip[E2 >: E <: Throwable, B](that: Attempt[E2, B]): Attempt[E2, (A, B)] =
    zipWith(that)(zipWithTuple2Fun)(parasitic)

  def zipWith[E2 >: E <: Throwable, U, R](that: Attempt[E2, U])(f: (A, U) => R)(implicit
                                                                                ec: ExecutionContext
  ): Attempt[E2, R] =
    Attempt(self.toFuture.zipWith(that.toFuture)(f), isFatal || that.isFatal)

  def catchAll[E2 >: E <: Throwable, A2 >: A](f: E => Attempt[E2, A2])(implicit
                                                                       ec: ExecutionContext
  ): Attempt[E2, A2] = {
    var isFutureFatal     = isFatal
    val transformedFuture = self.toFuture.transformWith {
      case Failure(e) if NonFatal(e) && !isFatal => f(e.asInstanceOf[E]).toFuture
      case Failure(e) if !NonFatal(e) || isFatal =>
        isFutureFatal = true
        self.toFuture
      case _                                     => self.toFuture
    }
    Attempt[E2, A2](transformedFuture, isFutureFatal)
  }

  def catchSome[E2 >: E <: Throwable, A2 >: A](pf: PartialFunction[E, Attempt[E2, A2]])(implicit
                                                                                        ec: ExecutionContext
  ): Attempt[E2, A2] = {
    val transformedFuture = self.toFuture.transformWith {
      case Failure(e) if NonFatal(e) && pf.isDefinedAt(e.asInstanceOf[E]) && !isFatal =>
        pf(e.asInstanceOf[E]).toFuture
      case _                                                                          =>
        self.toFuture
    }
    Attempt[E2, A2](transformedFuture, isFatal)
  }

  private final def failedFun[B](v: Try[B]): Try[E] =
    v match {
      case Failure(e) if NonFatal(e) && !isFatal => Success(e.asInstanceOf[E])
      case Failure(exception)                    => Failure(exception)
      case Success(_)                            => failedFailure
    }

  def failed: Attempt[NoSuchElementException, E] =
    transform(failedFun)(parasitic).asInstanceOf[Attempt[NoSuchElementException, E]]

  def foreach[U](f: A => U)(implicit executor: ExecutionContext): Unit =
    onComplete(_ foreach f)

  def onComplete[B](f: Try[A] => B)(implicit executor: ExecutionContext): Unit =
    self.toFuture.onComplete(f)

  def isCompleted: Boolean = self.toFuture.isCompleted

  def transform[B](f: Try[A] => Try[B])(implicit executor: ExecutionContext): Attempt[Throwable, B] =
    Attempt(self.toFuture.transform(f), isFatal)

  def transformWith[E2 >: E <: Throwable, B](f: Try[A] => Attempt[E2, B])(implicit
                                                                          executor: ExecutionContext
  ): Attempt[E2, B] =
    Attempt(self.toFuture.transformWith(f(_).toFuture), isFatal)

  @throws(classOf[TimeoutException])
  @throws(classOf[InterruptedException])
  def ready(atMost: Duration)(implicit permit: CanAwait): this.type = {
    self.toFuture.ready(atMost)
    this
  }

  @throws(classOf[TimeoutException])
  @throws(classOf[InterruptedException])
  def result(atMost: Duration)(implicit permit: CanAwait): A =
    self.toFuture.result(atMost)

  def recover[B >: A](pf: PartialFunction[E, B])(implicit executor: ExecutionContext): Attempt[E, B] =
    transform(_.recover(pf.asInstanceOf[PartialFunction[Throwable, B]]))
      .asInstanceOf[Attempt[E, B]]
}

object Attempt {
  def unapply[E <: Throwable, A](result: Attempt[E, A]): Option[(StdFuture[A], Boolean)] =
    Some((result.toFuture, result.isFatal))

  private[ragazoor] final def apply[E <: Throwable, A](future: StdFuture[A], fatal: Boolean): Attempt[E, A] =
    new Attempt[E, A] {
      override def toFuture: StdFuture[A] = future
      override val isFatal: Boolean       = fatal
    }

  final def apply[A](body: => A)(implicit ec: ExecutionContext): Attempt[Throwable, A] =
    Attempt[Throwable, A](StdFuture(body), fatal = false)

  final def fromFuture[A](future: StdFuture[A]): Attempt[Throwable, A] =
    Attempt(future, fatal = false)

  final def fromEither[E <: Throwable, A](either: Either[E, A]): Attempt[E, A] =
    Attempt(StdFuture.fromTry(either.toTry), fatal = false)

  final def fromTry[A](result: Try[A]): Attempt[Throwable, A] =
    Attempt[Throwable, A](StdFuture.fromTry(result), fatal = false)

  final def successful[A](result: A): Attempt[Nothing, A] = {
    val future = StdFuture.successful(result)
    new Attempt[Nothing, A] {
      override def toFuture: StdFuture[A] = future
      override val isFatal: Boolean       = false
    }
  }

  final def failed[E <: Exception](exception: E): Attempt[E, Nothing] = {
    val future = StdFuture.failed(exception)
    new Attempt[E, Nothing] {
      override def toFuture: StdFuture[Nothing] = future
      override val isFatal: Boolean             = false
    }
  }

  final def fatal(exception: Exception): Attempt[Nothing, Nothing] = {
    val future = StdFuture.failed(exception)
    new Attempt[Nothing, Nothing] {
      override def toFuture: StdFuture[Nothing] = future
      override val isFatal: Boolean             = true
    }
  }

  final def sequence[E <: Throwable, A](results: Seq[Attempt[E, A]])(implicit
                                                                     ec: ExecutionContext
  ): Attempt[E, Seq[A]] =
    Attempt(StdFuture.sequence(results.map(_.toFuture)), fatal = false)

  val unit = Attempt.successful(())
}
