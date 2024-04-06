package io.github.ragazoor

import scala.concurrent.{ Future => StdFuture }
import scala.util.Try

/*
 * Testing this to see if it is possible to make it completely drop in
 */
object Future {

  def unapply[E <: Throwable, A](result: IO[E, A]): Option[StdFuture[A]] =
    IO.unapply(result).map(_._1)

  final def apply[A](body: => A)(implicit ec: ExecutionContext): IO[Throwable, A] =
    IO[Throwable, A](StdFuture(body), fatal = false)

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

  final def sequence[E <: Throwable, A](results: Seq[IO[E, A]])(implicit
    ec: ExecutionContext
  ): IO[E, Seq[A]] =
    IO.sequence(results)

  val unit = IO.unit

}
