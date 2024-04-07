import io.github.ragazoor.Future
import io.github.ragazoor.implicits._

class UserServiceFuture(userRepo: UserRepository) {
  def getUser(id: Int): Future[User] = // Future[User] is an alias for Attempt[Throwable, User]
    userRepo
      .getUser(id)
      .attempt // Converts to Attempt
}
