import io.github.ragazoor.Attempt
import io.github.ragazoor.implicits._

import scala.concurrent.ExecutionContext


class UserServiceAttempt(userRepo: UserRepository)(implicit ec: ExecutionContext) {
  def getUser(id: Int): Attempt[UserNotFound, User] =
    userRepo
      .getUser(id)
      .attempt // Converts to Attempt
      .mapError(e => UserNotFound(s"User with id $id not found", e)) // Converts Error from Throwable -> UserNotFound
}
