import scala.concurrent.{Future => StdFuture}


trait UserRepository {
  def getUser(id: Int): StdFuture[User]
}
