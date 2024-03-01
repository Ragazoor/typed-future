package dev.ragz.io

import scala.concurrent.{ Future => StdFuture }
import scala.language.implicitConversions

object implicits {
  implicit def stdFutureToIO[A](future: StdFuture[A]): IO[Throwable, A] =
    IO.fromFuture(future)
}
