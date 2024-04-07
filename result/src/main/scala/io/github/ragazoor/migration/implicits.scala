package io.github.ragazoor.migration

import io.github.ragazoor.Attempt

import scala.concurrent.{Future => StdFuture}
import scala.language.implicitConversions

object implicits extends IoToStdFuture {

  implicit def ioToStdFuture[E <: Throwable, A](io: Attempt[E, A]): StdFuture[A] =
    io.toFuture

}
