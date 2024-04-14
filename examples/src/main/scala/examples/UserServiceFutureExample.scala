package examples

import common.User
import io.github.ragazoor.Future
import io.github.ragazoor.implicits.StdFutureToTask
import io.github.ragazoor.migration.implicits._

import scala.concurrent.{ Future => StdFuture }

/*
 * Imagine this is in a third party library
 */
trait UserProcess {
  def process(id: StdFuture[User]): StdFuture[User]
}

class UserServiceFutureExample(userProcess: UserProcess) {

  /* implicit conversion in io.github.ragazoor.migration.implicits._ converts
   * the Task to a Future
   */
  def proccessUser(user: Future[User]): Future[User] =
    userProcess.process(user).toTask

  // Does the same thing without implicits, but more migration needed
  def getUserExplicit(user: Future[User]): Future[User] =
    userProcess.process(user.toFuture).toTask
}
