package examples

import common.{ User, UserNotFound, UserRepository }
import io.github.ragazoor.Task
import io.github.ragazoor.implicits.StdFutureToTask

import scala.concurrent.ExecutionContext

class UserServiceTaskExample(userRepo: UserRepository)(implicit ec: ExecutionContext) {
  def getUser(id: Int): Task[UserNotFound, User] =
    userRepo
      .getUser(id)
      .toTask // Converts to Task
      .mapError(e =>
        UserNotFound(s"common.User with id $id not found", e)
      )       // Converts Error from Throwable -> UserNotFound
}
