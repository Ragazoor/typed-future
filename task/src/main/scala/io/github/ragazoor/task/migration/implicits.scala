package io.github.ragazoor.task.migration

import io.github.ragazoor.task.Task

import scala.language.implicitConversions

object implicits extends TaskToFuture {

  implicit def taskToFuture[E <: Throwable, A](task: Task[E, A]): scala.concurrent.Future[A] =
    task.toFuture

}
