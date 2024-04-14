package examples

import common.{User, UserRepository}
import io.github.ragazoor.Future
import io.github.ragazoor.implicits.StdFutureToTask

class UserServiceExample(userRepo: UserRepository) {
  def getUser(id: Int): Future[User] = // Future[User] is an alias for Task[Throwable, User]
    userRepo
      .getUser(id)
      .toTask // Converts to Task
}
