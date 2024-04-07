package io.github.ragazoor

import scala.concurrent.{ Future => StdFuture }

object implicits {
  implicit class StdFutureToIo[A](val future: StdFuture[A]) {
    def io: Attempt[Throwable, A] = Attempt.fromFuture(future)
  }

}
