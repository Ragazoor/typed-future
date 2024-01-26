package example

import scala.concurrent.Future
import scala.concurrent.CanAwait
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext
import scala.util.Either
import scala.util.Try
import scala.util.control.NonFatal
import scala.concurrent.impl.Promise
import scala.util.Success

trait Result[+E, +A] extends Future[A]

final case class Ok[+A](value: A) extends Result[Nothing, A]

final case class Panic[+E](value: E) extends Result[E, Nothing]

object Result {

  val b = Future.successful(1)

  final def apply[E<:Throwable,T](body: => T)(implicit ec: ExecutionContext): Result[E, T] =
    Future.unit.map(_ => body)
}