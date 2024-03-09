package dev.ragz.io.migration

import scala.concurrent.{ Future => StdFuture }
import scala.language.implicitConversions
import dev.ragz.io.IO

object implicits extends IoToStdFuture {

  implicit def ioToStdFuture[E <: Throwable, A](io: IO[E, A]): StdFuture[A] =
    io.toFuture

}
