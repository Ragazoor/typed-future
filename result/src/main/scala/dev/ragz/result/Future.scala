package dev.ragz.result

import scala.concurrent.{ExecutionContext, Future => StdFuture}
import scala.util.Try

object Future {

  def unapply[E <: Throwable, A](result: Result[E, A]): Option[StdFuture[A]] =
    Result.unapply(result)

  private final def apply[E <: Throwable, A](future: StdFuture[A]): Result[E, A] =
    Result(future)

  final def apply[A](body: => A)(implicit ec: ExecutionContext): Result[Throwable, A] =
    Result[Throwable, A](StdFuture(body))

  final def fromFuture[A](future: StdFuture[A]): Result[Throwable, A] =
    Result.fromFuture(future)

  final def fromEither[E <: Throwable, A](either: Either[E, A]): Result[E, A] =
    Result.fromEither(either)

  final def fromTry[A](body: Try[A]): Result[Throwable, A] =
    Result.fromTry(body)

  final def successful[A](value: A): Result[Nothing, A] =
    Result.successful(value)

  final def failed[E <: Exception](exception: E): Result[E, Nothing] =
    Result.failed(exception)

  final def fatal(exception: Throwable): Result[FatalError, Nothing] =
    Result.fatal(exception)

  final def sequence[E <: Throwable, A](results: Seq[Result[E, A]])(implicit
                                                                    ec: ExecutionContext
  ): Result[E, Seq[A]] =
    Result.sequence(results)

}
