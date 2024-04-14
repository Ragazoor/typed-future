package io.github.ragazoor

import scala.concurrent.{ Future => StdFuture }

object implicits {
  implicit class StdFutureToTask[A](val future: StdFuture[A]) {
    def toTask: Task[Throwable, A] = Task.fromFuture(future)
  }

}
