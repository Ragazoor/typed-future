import io.github.ragazoor.Attempt
import io.github.ragazoor.implicits.StdFutureToIo

import scala.concurrent.{ExecutionContext, Future => StdFuture}

object ImplicitClassExample {
  implicit class MyImplicitClassFunction(f: StdFuture[Int])(implicit ec: ExecutionContext) {
    def bar: StdFuture[Option[Int]] = f.map(Some(_))
  }

  import scala.concurrent.ExecutionContext.Implicits.global
  def foo: Attempt[Throwable, Int] = ???
  // does not compile
  // val a: Attempt[Throwable, Option[Int]] = foo.bar.attempt
  val b: Attempt[Throwable, Option[Int]] = foo.toFuture.bar.attempt
}
