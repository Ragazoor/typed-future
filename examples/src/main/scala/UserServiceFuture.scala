import io.github.ragazoor.Future
import io.github.ragazoor.implicits._

import scala.concurrent.{Future => StdFuture}

case class User(name: String, age: Int)

trait UserRepository {
  def getUser(id: Int): StdFuture[User]
}

class UserService(userRepo: UserRepository) {
  def getUser(id: Int): Future[User] = // Future[User] is an alias for Attempt[Throwable, User]
    userRepo
      .getUser(id)
      .attempt // Converts to Attempt
}
