package dev.ragz.io

import scala.concurrent.{ Future => StdFuture }
import scala.language.implicitConversions

object implicits {
  implicit class StdFutureToIo[A](val future: StdFuture[A]) {
    def io: IO[Throwable, A] = IO.fromFuture(future)
  }

  implicit def ioToStdFuture[E <: Throwable, A](io: IO[E, A]): StdFuture[A] =
    io.toFuture

  implicit def ioToStdFutureF0[E <: Throwable, A](f0: () => IO[E, A]): () => StdFuture[A] =
    () => f0().toFuture

  implicit def ioToStdFutureF1[E <: Throwable, A, B](f1: A => IO[E, B]): A => StdFuture[B] =
    x1 => f1(x1).toFuture
}
