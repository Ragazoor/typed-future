package dev.ragz.result

import scala.concurrent.ExecutionContext.parasitic
import scala.concurrent.{ ExecutionContext, Future => StdFuture }
import scala.util.{ Failure, Try }

trait Future[+E <: Throwable, +A] {
  self =>
  def toFuture: StdFuture[A]

  def value: Option[Try[A]] = self.toFuture.value

  def map[B](f: A => B)(implicit ec: ExecutionContext): Future[E, B] =
    Future(self.toFuture.transform(_ map f))

  def flatMap[E2 >: E <: Throwable, B](f: A => Future[E2, B])(implicit ec: ExecutionContext): Future[E2, B] =
    Future(self.toFuture.flatMap(f(_).toFuture))

  def flatten[E2 >: E <: Throwable, B](implicit ev: A <:< Future[E2, B]): Future[E2, B] =
    flatMap(ev)(parasitic)

  def mapError[E2 <: Throwable](f: E => E2)(implicit ec: ExecutionContext): Future[E2, A] =
    Future[E2, A] {
      self.toFuture.transform {
        case Failure(e) => Failure(f(e.asInstanceOf[E]))
        case success    => success
      }
    }

  def zip[E2 >: E <: Throwable, B](that: Future[E2, B]): Future[E2, (A, B)] =
    zipWith(that)(Future.zipWithTuple2Fun)(parasitic)

  def zipWith[E2 >: E <: Throwable, U, R](that: Future[E2, U])(f: (A, U) => R)(implicit
    ec: ExecutionContext
  ): Future[E2, R] =
    Future(self.toFuture.zipWith(that.toFuture)(f))

  def catchAll[E2 >: E <: Throwable, A2 >: A](f: E => Future[E2, A2])(implicit
    ec: ExecutionContext
  ): Future[E2, A2] =
    Future[E2, A2] {
      self.toFuture.transformWith {
        case Failure(e) if ResultNonFatal(e) => f(e.asInstanceOf[E]).toFuture
        case _                               => self.toFuture
      }
    }

  def catchSome[E2 >: E <: Throwable, A2 >: A](pf: PartialFunction[E, Future[E2, A2]])(implicit
    ec: ExecutionContext
  ): Future[E2, A2] =
    Future[E2, A2] {
      self.toFuture.transformWith {
        case Failure(e) if ResultNonFatal(e) && pf.isDefinedAt(e.asInstanceOf[E]) =>
          pf(e.asInstanceOf[E]).toFuture
        case _                                                                    =>
          self.toFuture
      }
    }
}

object Future {

  private final case class Attempt[+E <: Throwable, +A] private (future: StdFuture[A]) extends Future[E, A] {
    override def toFuture: StdFuture[A] = future
  }

  private final case class Success[+A] private (success: A) extends Future[Nothing, A] {
    override def toFuture: StdFuture[A] = StdFuture.successful(success)
  }

  private final case class Failed[+E <: Exception] private (failure: E) extends Future[E, Nothing] {
    override def toFuture: StdFuture[Nothing] = StdFuture.failed(failure)
  }

  private final case class Fatal private (failure: Throwable) extends Future[FatalError, Nothing] {
    override def toFuture: StdFuture[Nothing] = StdFuture.failed(FatalError(failure))
  }

  private final val _zipWithTuple2: (Any, Any) => (Any, Any) = Tuple2.apply

  private[result] final def zipWithTuple2Fun[T, U]: (T, U) => (T, U) =
    _zipWithTuple2.asInstanceOf[(T, U) => (T, U)]

  def unapply[E <: Throwable, A](result: Future[E, A]): Option[StdFuture[A]] =
    Some(result.toFuture)

  private final def apply[E <: Throwable, A](future: StdFuture[A]): Future[E, A] =
    Attempt(future)

  final def apply[A](body: => A)(implicit ec: ExecutionContext): Future[Throwable, A] =
    Future[Throwable, A](StdFuture(body))

  final def fromFuture[A](future: StdFuture[A]): Future[Throwable, A] =
    Future(future)

  final def fromEither[E <: Throwable, A](either: Either[E, A]): Future[E, A] =
    Future(StdFuture.fromTry(either.toTry))

  final def fromTry[A](body: Try[A]): Future[Throwable, A] =
    Future[Throwable, A](StdFuture.fromTry(body))

  final def successful[A](value: A): Future[Nothing, A] =
    Success(value)

  final def failed[E <: Exception](exception: E): Future[E, Nothing] =
    Failed(exception)

  final def fatal(exception: Throwable): Future[FatalError, Nothing] =
    Fatal(exception)

  final def sequence[E <: Throwable, A](results: Seq[Future[E, A]])(implicit
    ec: ExecutionContext
  ): Future[E, Seq[A]] =
    Attempt(StdFuture.sequence(results.map(_.toFuture)))

}
