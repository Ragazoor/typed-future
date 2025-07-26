package io.github.ragazoor.task.examples.migration

import io.github.ragazoor.task.examples.utils.User
import io.github.ragazoor.task.implicits.FutureToTask

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Your current code based on `scala.concurrent.Future`.
 */
class UserRepositoryImpl() {
  def getUser(id: Int): scala.concurrent.Future[User] =
    // Simulate fetching a user
    scala.concurrent.Future.successful(User(s"Stefan", id))
}

object MigrationMain extends App {

  val userRepo = new UserRepositoryImpl()
  val userTask = userRepo.getUser(1).toTask // Convert Future to Task

  userTask.foreach { user =>
    println(s"User fetched: $user")
  }

  Await.ready(userTask, scala.concurrent.duration.Duration.Inf)
}
