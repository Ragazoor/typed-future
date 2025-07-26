package io.github.ragazoor.task.examples.basic

import io.github.ragazoor.task.examples.interop.FutureService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

object BasicMain extends App {
  private val userRepo    = new UserRepositoryImpl()
  private val userService = new UserService(userRepo)

  private val userTask = userService.getUser(id = 1, authToken = "valid-token")
  userTask.onComplete {
    case Success(user)      => println(s"User fetched: $user")
    case Failure(exception) => println(s"Exception: ${exception.getMessage}")
  }
  FutureService.await(userTask.toFuture)
}
