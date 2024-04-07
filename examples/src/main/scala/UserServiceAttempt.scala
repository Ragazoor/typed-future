import io.github.ragazoor.Attempt
import io.github.ragazoor.implicits._

import scala.concurrent.{ ExecutionContext, Future => StdFuture }

case class User(name: String, age: Int)

trait UserRepository {
  def getUser(id: Int): StdFuture[User]
}

final case class UserNotFound(msg: String, cause: Throwable) extends Exception(msg, cause)

class UserService(userRepo: UserRepository)(implicit ec: ExecutionContext) {
  def getUser(id: Int): Attempt[UserNotFound, User] =
    userRepo
      .getUser(id)
      .attempt // Converts to Attempt
      .mapError(e => UserNotFound(s"User with id $id not found", e)) // Converts Error from Throwable -> UserNotFound
}
