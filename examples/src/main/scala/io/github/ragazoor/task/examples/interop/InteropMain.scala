package io.github.ragazoor.task.examples.interop

import io.github.ragazoor.task.examples.basic.UserRepositoryImpl
import scala.concurrent.ExecutionContext.Implicits.global

object InteropMain extends App {
  val userRepo = new UserRepositoryImpl()
  val userTask = userRepo.getUser(1)

  val user = FutureService.await(userTask.toFuture)
  println(s"User fetched: $user")
}

object InteropMain2 extends App {
  val userRepo = new UserRepositoryImpl()
  val userTask = userRepo.getUser(1)

  // importing this implicit allows to use .await() without explicitly converting to Future
  import io.github.ragazoor.task.migration.implicits.taskToFuture
  val user = FutureService.await(userTask)
  println(s"User fetched: $user")
}
