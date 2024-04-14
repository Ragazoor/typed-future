package examples

import io.github.ragazoor.Task
import io.github.ragazoor.implicits.StdFutureToTask

import scala.concurrent.{ExecutionContext, Future => StdFuture}

object ImplicitClassExample {
  implicit class MyImplicitClassFunction[A](f: StdFuture[A])(implicit ec: ExecutionContext) {
    def bar: StdFuture[Option[A]] = f.map(Some(_))
  }
  def foo: Task[Throwable, Int] = ???
  /* does not compile */
  // val a: Task[Throwable, Option[Int]] = foo.bar.toTask

  import scala.concurrent.ExecutionContext.Implicits.global
  val b: Task[Throwable, Option[Int]] = foo.toFuture.bar.toTask
}
