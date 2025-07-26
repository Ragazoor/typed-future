package io.github.ragazoor.task.examples.basic

import io.github.ragazoor.task.implicits.FutureToTask
import io.github.ragazoor.task.Task
import io.github.ragazoor.task.examples.utils.User
import io.github.ragazoor.task.examples.utils.exceptions.UserNotFoundException

import scala.concurrent.ExecutionContext

class UserRepositoryImpl(implicit ec: ExecutionContext) {

  /**
   * Simulates fetching a user by ID using a scala.concurrent.Future based library. In a real application, this would
   * likely involve a database query or an API call. If the Task fails with a non fatal error it will be converted to a
   * UserNotFound error.
   *
   * @param id
   * @return
   */
  def getUser(id: Int): Task[UserNotFoundException, User] =
    scala.concurrent.Future {
      if (id == 1) User("John Doe", 55)
      else throw new Exception(s"User with id $id not found")
    }.toTask
      .mapError(e => UserNotFoundException(e.getMessage, e))

}
