package io.github.ragazoor

import scala.concurrent.{ExecutionContext, Future => StdFuture}
import scala.util.Try

/*
 * Testing this to see if it is possible to make it completely drop in
 */
object Future {

  def unapply[E <: Throwable, A](result: Task[E, A]): Option[StdFuture[A]] =
    Task.unapply(result)

  final def apply[A](body: => A)(implicit ec: ExecutionContext): Task[Throwable, A] =
    Task[Throwable, A](StdFuture(body))

  final def fromFuture[A](future: StdFuture[A]): Task[Throwable, A] =
    Task.fromFuture(future)

  final def fromEither[E <: Throwable, A](either: Either[E, A]): Task[E, A] =
    Task.fromEither(either)

  final def fromTry[A](body: Try[A]): Task[Throwable, A] =
    Task.fromTry(body)

  final def successful[A](value: A): Task[Nothing, A] =
    Task.successful(value)

  final def failed[E <: Exception](exception: E): Task[E, Nothing] =
    Task.failed(exception)

  final def sequence[E <: Throwable, A](results: Seq[Task[E, A]])(implicit
                                                                  ec: ExecutionContext
  ): Task[E, Seq[A]] =
    Task.sequence(results)

  val unit = Task.unit

}
