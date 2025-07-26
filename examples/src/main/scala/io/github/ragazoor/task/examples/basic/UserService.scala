package io.github.ragazoor.task.examples.basic

import io.github.ragazoor.task.Task
import io.github.ragazoor.task.examples.utils.User
import io.github.ragazoor.task.examples.utils.exceptions.{ DomainException, NotAllowedException }

/**
 * Toy example to show how Task's with different failure types composes.
 *
 * @param userRepo
 */

class UserService(userRepo: UserRepositoryImpl) {

  def getUser(id: Int, authToken: String): Task[DomainException, User] =
    if (authToken == "valid-token")
      userRepo.getUser(id) // Returns Task[UserNotFoundException, User]
    else
      Task.failed(NotAllowedException("Invalid authentication token"))

}
