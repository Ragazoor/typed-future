package example

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

final class Result[+E <: Throwable, +A] private[example] (future: Future[A]) extends ResultT[E, A] {
  override def toFuture: Future[A] = future
}

final class Success[+A] private[example] (success: A) extends ResultT[Nothing, A] {
  override def toFuture: Future[A] = Future.successful(success)
}

final class Failed[+E <: Exception] private[example] (failure: E) extends ResultT[E, Nothing] {
  override def toFuture: Future[Nothing] = Future.failed(failure)
}

final class Fatal[+E <: Throwable] private[example] (failure: E) extends ResultT[E, Nothing] {
  override def toFuture: Future[Nothing] = Future.failed(failure)
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

  final def succeed[A](value: A): Result[Nothing, A] =
    Result(Future.successful(value))

  final def failed[E <: Throwable](exception: E): Result[E, Nothing] =
    Result(Future.failed(exception))

  final def fatal[E <: Throwable](exception: E): Result[E, Nothing] =
    Result(Future.failed(exception))

  final def sequence[E <: Throwable, A](results: Seq[Result[E, A]])(implicit
    ec: ExecutionContext
  ): Result[E, Seq[A]] =
    new Result(Future.sequence(results.map(_.toFuture)))

}
