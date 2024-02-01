package example

import scala.concurrent.ExecutionContext.parasitic
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try
import scala.util.Failure

final class Result[+E <: Throwable, +A] private[example] (future: Future[A]) {
  def toFuture: Future[A] = future

  // TODO: Fatal Error on exception in f
  // Another with A => Try[B] to catch error
  def map[B](f: A => B)(implicit ec: ExecutionContext): Result[E, B] =
    Result[E, B](future.map(f))

  def mapError[E2 <: Throwable](f: E => E2)(implicit ec: ExecutionContext): Result[E2, A] = {
    Result(future.transform {
      case Failure(e: E) => Failure(f(e))
      case success              => success
    })
  }

  def flatMap[E2 >: E <: Throwable, B](f: A => Result[E2, B])(implicit ec: ExecutionContext): Result[E2, B] = {
    val newFuture =
      for {
        a <- future
        b <- f(a).toFuture
      } yield b
    Result[E2, B](newFuture)
  }

  def flatten[E2 >: E <: Throwable, B](implicit ev: A <:< Result[E2, B]): Result[E2, B] =
    flatMap(ev)(parasitic)

  def onComplete(f: Try[A] => Unit)(implicit ec: ExecutionContext): Unit =
    future.onComplete(f)

  def zip[E2 >: E <: Throwable, B](that: Result[E2, B]): Result[E2, (A, B)] =
    zipWith(that)(Result.zipWithTuple2Fun)(parasitic)

  def zipWith[E2 >: E <: Throwable, U, R](that: Result[E2, U])(f: (A, U) => R)(implicit
    ec: ExecutionContext
  ): Result[E2, R] =
    Result(future.zipWith(that.toFuture)(f))

  def recoverWith[E2 >: E <: Throwable, B >: A](
    pf: PartialFunction[E, Result[E2, B]]
  )(implicit ec: ExecutionContext): Result[E2, B] =
    Result(
      future.recoverWith(
        pf
          .asInstanceOf[PartialFunction[Throwable, Result[E2, B]]]
          .andThen(_.toFuture)
      )
    )

  def recover[B >: A](pf: PartialFunction[E, B])(implicit ec: ExecutionContext): Result[E, B] =
    Result(future.recover(pf.asInstanceOf[PartialFunction[Throwable, B]]))

}

object Result {
  private final val _zipWithTuple2: (Any, Any) => (Any, Any) = Tuple2.apply _

  private[example] final def zipWithTuple2Fun[T, U]: (T, U) => (T, U) =
    _zipWithTuple2.asInstanceOf[(T, U) => (T, U)]

  def unapply[E <: Throwable, A](result: Result[E, A]): Option[Future[A]] =
    Some(result.toFuture)

  private[example] def apply[E <: Throwable, A](future: Future[A]): Result[E, A] =
    new Result(future)

  final def apply[A](body: => A)(implicit ec: ExecutionContext): Result[Throwable, A] =
    Result[Throwable, A](Future(body))

  final def fromFuture[A](future: Future[A]): Result[Throwable, A] =
    Result(future)

  final def fromTry[A](body: Try[A]): Result[Throwable, A] =
    Result[Throwable, A](Future.fromTry(body))

  final def successful[A](value: A): Result[Nothing, A] =
    Result(Future.successful(value))

  final def failed[E <: Throwable](exception: E): Result[E, Nothing] =
    Result(Future.failed(exception))

  final def sequence[E <: Throwable, A](results: Seq[Result[E, A]])(implicit
    ec: ExecutionContext
  ): Result[E, Seq[A]] =
    new Result(Future.sequence(results.map(_.toFuture)))

}
