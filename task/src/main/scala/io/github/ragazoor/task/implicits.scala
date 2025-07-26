package io.github.ragazoor.task

object implicits {
  implicit class FutureToTask[A](val future: scala.concurrent.Future[A]) {
    def toTask: Task[Throwable, A] = Task.fromFuture(future)
  }

}
