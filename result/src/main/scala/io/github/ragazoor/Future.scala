package io.github.ragazoor

import scala.concurrent.{ExecutionContext, Future => StdFuture}
import scala.util.Try

/*
 * Testing this to see if it is possible to make it completely drop in
 */
object Future {

  def unapply[E <: Throwable, A](result: Attempt[E, A]): Option[StdFuture[A]] =
    Attempt.unapply(result).map(_._1)

  final def apply[A](body: => A)(implicit ec: ExecutionContext): Attempt[Throwable, A] =
    Attempt[Throwable, A](StdFuture(body), fatal = false)

  final def fromFuture[A](future: StdFuture[A]): Attempt[Throwable, A] =
    Attempt.fromFuture(future)

  final def fromEither[E <: Throwable, A](either: Either[E, A]): Attempt[E, A] =
    Attempt.fromEither(either)

  final def fromTry[A](body: Try[A]): Attempt[Throwable, A] =
    Attempt.fromTry(body)

  final def successful[A](value: A): Attempt[Nothing, A] =
    Attempt.successful(value)

  final def failed[E <: Exception](exception: E): Attempt[E, Nothing] =
    Attempt.failed(exception)

  final def sequence[E <: Throwable, A](results: Seq[Attempt[E, A]])(implicit
    ec: ExecutionContext
  ): Attempt[E, Seq[A]] =
    Attempt.sequence(results)

  val unit = Attempt.unit

}
