package examples

import common.User
import io.github.ragazoor.implicits.StdFutureToTask
import io.github.ragazoor.migration.implicits._
import io.github.ragazoor.{ Future, Task }

import scala.concurrent.{ ExecutionContext, Future => StdFuture }

/*
 * Imagine this is in a third party library
 */
trait UserProcess {
  def process(id: StdFuture[User]): StdFuture[User]
}

class UserServiceFutureExample(userProcess: UserProcess)(implicit ec: ExecutionContext) {

  /* implicit conversion in io.github.ragazoor.migration.implicits._ converts
   * the Task to a Future
   */
  def getUser(id: Int): Future[User] =
    userProcess.process {
      Task.successful(User("Test name", 44))
    }.toTask

  // Does the same thing without implicits, but more migration needed
  def getUserExplicit(id: Int): Future[User] =
    userProcess.process {
      Task.successful(User("Test name", 44)).toFuture // Here the conversion to future is explicit
    }.toTask
}
