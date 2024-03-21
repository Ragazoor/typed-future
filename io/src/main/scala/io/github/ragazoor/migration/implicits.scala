package io.github.ragazoor.migration

import scala.concurrent.{ Future => StdFuture }
import scala.language.implicitConversions
import io.github.ragazoor.IO

object implicits extends IoToStdFuture {

  implicit def ioToStdFuture[E <: Throwable, A](io: IO[E, A]): StdFuture[A] =
    io.toFuture

}
