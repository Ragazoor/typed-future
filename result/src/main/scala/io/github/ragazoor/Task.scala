package io.github.ragazoor

import io.github.ragazoor.TaskUtils.{ failedFailure, zipWithTuple2Fun }

import scala.concurrent.ExecutionContext.parasitic
import scala.concurrent.duration.Duration
import scala.concurrent.{ Awaitable, CanAwait, ExecutionContext, Future => StdFuture, TimeoutException }
import scala.util.control.NonFatal
import scala.util.{ Failure, Success, Try }

sealed trait Task[+E <: Throwable, +A] extends Awaitable[A] {
  self =>
  def toFuture: StdFuture[A]

  def value: Option[Try[A]] = self.toFuture.value

  def map[B](f: A => B)(implicit ec: ExecutionContext): Task[E, B] =
    Task(self.toFuture.transform(_ map f))

  def mapTry[B](f: A => Try[B])(implicit ec: ExecutionContext): Task[Throwable, B] =
    Task(self.toFuture.transform(_ flatMap f))

  def mapEither[E2 >: E <: Throwable, B](f: A => Either[E2, B])(implicit ec: ExecutionContext): Task[E2, B] =
    Task(self.toFuture.transform(_ flatMap (f(_).toTry)))

  def flatMap[E2 >: E <: Throwable, B](f: A => Task[E2, B])(implicit ec: ExecutionContext): Task[E2, B] =
    Task(self.toFuture.flatMap(f(_).toFuture))

  def flatten[E2 >: E <: Throwable, B](implicit ev: A <:< Task[E2, B]): Task[E2, B] =
    flatMap(ev)(parasitic)

  def mapError[E2 <: Throwable](f: E => E2)(implicit ec: ExecutionContext): Task[E2, A] = {
    val transformedFuture = self.toFuture.transform {
      case Failure(e) if NonFatal(e)  => Failure(f(e.asInstanceOf[E]))
      case Failure(e) if !NonFatal(e) =>
        Failure(e)
      case success                    => success
    }
    Task[E2, A](transformedFuture)
  }

  def zip[E2 >: E <: Throwable, B](that: Task[E2, B]): Task[E2, (A, B)] =
    zipWith(that)(zipWithTuple2Fun)(parasitic)

  def zipWith[E2 >: E <: Throwable, U, R](that: Task[E2, U])(f: (A, U) => R)(implicit
    ec: ExecutionContext
  ): Task[E2, R] =
    Task(self.toFuture.zipWith(that.toFuture)(f))

  def catchAll[E2 >: E <: Throwable, A2 >: A](f: E => Task[E2, A2])(implicit
    ec: ExecutionContext
  ): Task[E2, A2] = {
    val transformedFuture = self.toFuture.transformWith {
      case Failure(e) if NonFatal(e)  => f(e.asInstanceOf[E]).toFuture
      case Failure(e) if !NonFatal(e) =>
        self.toFuture
      case _                          => self.toFuture
    }
    Task[E2, A2](transformedFuture)
  }

  def catchSome[E2 >: E <: Throwable, A2 >: A](pf: PartialFunction[E, Task[E2, A2]])(implicit
    ec: ExecutionContext
  ): Task[E2, A2] = {
    val transformedFuture = self.toFuture.transformWith {
      case Failure(e) if NonFatal(e) && pf.isDefinedAt(e.asInstanceOf[E]) =>
        pf(e.asInstanceOf[E]).toFuture
      case _                                                              =>
        self.toFuture
    }
    Task[E2, A2](transformedFuture)
  }

  private final def failedFun[B](v: Try[B]): Try[E] =
    v match {
      case Failure(e) if NonFatal(e) => Success(e.asInstanceOf[E])
      case Failure(exception)        => Failure(exception)
      case Success(_)                => failedFailure
    }

  def failed: Task[NoSuchElementException, E] =
    transform(failedFun)(parasitic).asInstanceOf[Task[NoSuchElementException, E]]

  def foreach[U](f: A => U)(implicit executor: ExecutionContext): Unit =
    onComplete(_ foreach f)

  def onComplete[B](f: Try[A] => B)(implicit executor: ExecutionContext): Unit =
    self.toFuture.onComplete(f)

  def isCompleted: Boolean = self.toFuture.isCompleted

  def transform[B](f: Try[A] => Try[B])(implicit executor: ExecutionContext): Task[Throwable, B] =
    Task(self.toFuture.transform(f))

  def transformWith[E2 >: E <: Throwable, B](f: Try[A] => Task[E2, B])(implicit
    executor: ExecutionContext
  ): Task[E2, B] = {
    val transformedFuture = toFuture.transformWith { value =>
      val newAttempt = f(value)
      newAttempt.toFuture
    }
    Task(transformedFuture)
  }

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

  def recover[B >: A](pf: PartialFunction[Throwable, B])(implicit executor: ExecutionContext): Task[E, B] =
    Task(toFuture.recover(pf)).asInstanceOf[Task[E, B]]

  def recoverWith[B >: A](pf: PartialFunction[Throwable, Task[Throwable, B]])(implicit
    executor: ExecutionContext
  ): Task[Throwable, B] =
    Task(toFuture.recoverWith(pf.andThen(_.toFuture)))

}

object Task {
  def unapply[E <: Throwable, A](result: Task[E, A]): Option[StdFuture[A]] =
    Some(result.toFuture)

  private[ragazoor] final def apply[E <: Throwable, A](future: StdFuture[A]): Task[E, A] =
    new Task[E, A] {
      override def toFuture: StdFuture[A] = future
    }

  final def apply[A](body: => A)(implicit ec: ExecutionContext): Task[Throwable, A] =
    Task[Throwable, A](StdFuture(body))

  final def fromFuture[A](future: StdFuture[A]): Task[Throwable, A] =
    Task(future)

  final def fromEither[E <: Throwable, A](either: Either[E, A]): Task[E, A] =
    Task(StdFuture.fromTry(either.toTry))

  final def fromTry[A](result: Try[A]): Task[Throwable, A] =
    Task[Throwable, A](StdFuture.fromTry(result))

  final def successful[A](result: A): Task[Nothing, A] = {
    val future = StdFuture.successful(result)
    new Task[Nothing, A] {
      override def toFuture: StdFuture[A] = future
    }
  }

  final def failed[E <: Exception](exception: E): Task[E, Nothing] = {
    val future = StdFuture.failed(exception)
    new Task[E, Nothing] {
      override def toFuture: StdFuture[Nothing] = future
    }
  }

  final def sequence[E <: Throwable, A](results: Seq[Task[E, A]])(implicit
    ec: ExecutionContext
  ): Task[E, Seq[A]] =
    Task(StdFuture.sequence(results.map(_.toFuture)))

  val unit = Task.successful(())
}
