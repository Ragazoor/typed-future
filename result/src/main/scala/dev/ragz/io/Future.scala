package dev.ragz.io

import scala.concurrent.{ExecutionContext, Future => StdFuture}
import scala.util.Try

object Future {

  def unapply[E <: Throwable, A](result: IO[E, A]): Option[StdFuture[A]] =
    IO.unapply(result)

  final def apply[A](body: => A)(implicit ec: ExecutionContext): IO[Throwable, A] =
    IO[Throwable, A](StdFuture(body))

  final def fromFuture[A](future: StdFuture[A]): IO[Throwable, A] =
    IO.fromFuture(future)

  final def fromEither[E <: Throwable, A](either: Either[E, A]): IO[E, A] =
    IO.fromEither(either)

  final def fromTry[A](body: Try[A]): IO[Throwable, A] =
    IO.fromTry(body)

  final def successful[A](value: A): IO[Nothing, A] =
    IO.successful(value)

  final def failed[E <: Exception](exception: E): IO[E, Nothing] =
    IO.failed(exception)

  final def fatal(exception: Throwable): IO[FatalError, Nothing] =
    IO.fatal(exception)

  final def sequence[E <: Throwable, A](results: Seq[IO[E, A]])(implicit
                                                                ec: ExecutionContext
  ): IO[E, Seq[A]] =
    IO.sequence(results)

}
