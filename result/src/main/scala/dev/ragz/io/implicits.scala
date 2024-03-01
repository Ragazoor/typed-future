package dev.ragz.io

import scala.concurrent.{ Future => StdFuture }
import scala.language.implicitConversions

object implicits extends IoToStdFuture {
  implicit class StdFutureToIo[A](val future: StdFuture[A]) {
    def io: IO[Throwable, A] = IO.fromFuture(future)
  }

  implicit def ioToStdFuture[E <: Throwable, A](io: IO[E, A]): StdFuture[A] =
    io.toFuture

}
