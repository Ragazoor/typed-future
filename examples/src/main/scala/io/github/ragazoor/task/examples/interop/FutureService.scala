package io.github.ragazoor.task.examples.interop

import io.github.ragazoor.task.examples.utils.User
import scala.concurrent.Await

/**
 * Imagine this is in a function from a third party library which takes a Future[?] as parameter. In this case we need
 * to convert our Task to Future before passing it to the function.
 */
object FutureService {
  def await(future: scala.concurrent.Future[User]): User =
    Await.result(future, scala.concurrent.duration.Duration.Inf)
}
