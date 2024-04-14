package io.github.ragazoor.migration

import io.github.ragazoor.Task

import scala.concurrent.{ Future => StdFuture }
import scala.language.implicitConversions

object implicits extends TaskToStdFuture {

  implicit def ioToStdFuture[E <: Throwable, A](io: Task[E, A]): StdFuture[A] =
    io.toFuture

}
