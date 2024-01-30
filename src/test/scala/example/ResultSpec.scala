package example

import scala.concurrent.Future
import munit.FunSuite

class HelloSpec extends FunSuite {
  import scala.concurrent.ExecutionContext.Implicits.global
  override def munitValueTransforms = super.munitValueTransforms ++ List(
    new ValueTransform(
      "Result",
      { case Result(future) =>
        future
      }
    )
  )
  test("Result using flatmap") {
    for {
      a <- Result.fromFuture(Future(1))
      b <- Result.fromFuture(Future(2))
      c <- Result.fromFuture(Future(3))
    } yield assertEquals(a + b + c, 6)
  }
  test("Result using flatmap with typed errors") {
    abstract class RootError(e: Throwable) extends Throwable(e)
    case class MyError(e: Throwable) extends RootError(e)
    case class MyError2(e: Throwable) extends RootError(e)
    case class MyError3(e: Throwable) extends Throwable(e)
    val a = for {
      a <- Result.fromFuture(MyError, Future(1))
      b <- Result.fromFuture(MyError2, Future(2))
      c <- Result.fromFuture(MyError3, Future.failed[Int](new RuntimeException))
    } yield assertEquals(a + b + c, 6)
    a.recover {
      case _: RuntimeException => assert(false)
      case _: MyError2         => assert(false)
      case _: MyError3         => assert(true)
    }
  }
  test("Result using sequence") {
    def getResult(i: Int) = Result.fromFuture(Future(i))
    Result
      .sequence(Seq(1, 2, 3).map(getResult))
      .map(a => a.reduce(_ + _))
      .map(sum => assertEquals(sum, 6))
  }

  test("Result fail with typed error") {
    def getResult(i: Int) =
      if (i < 2)
        Result.successful(i)
      else
        Result.failed(new IllegalArgumentException("test message"))

    Result
      .sequence(Seq(1, 2, 3).map(getResult))
      .recoverWith { case _: IllegalArgumentException =>
        Result.successful(assert(true))
      }
  }
}