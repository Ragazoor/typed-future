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

  implicit def ioToStdFutureF1[E <: Throwable, X1, B](f1: X1 => IO[E, B]): X1 => StdFuture[B] =
    x1 => f1(x1).toFuture

  implicit def ioToStdFutureF2[E <: Throwable, X1, X2, C](f2: (X1, X2) => IO[E, C]): (X1, X2) => StdFuture[C] =
    (x1, x2) => f2(x1, x2).toFuture

}
