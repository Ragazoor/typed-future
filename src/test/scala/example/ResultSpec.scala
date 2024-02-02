package example

import munit.FunSuite

import scala.concurrent.Future

class ResultSpec extends FunSuite {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def munitValueTransforms = super.munitValueTransforms ++ List(
    new ValueTransform(
      "Result",
      { case Result(future) =>
        future
      }
    )
  )

  abstract class RootError(e: Throwable) extends Exception(e)

  case class MyError(e: Throwable) extends RootError(e)

  case class MyError2(e: Throwable) extends RootError(e)

  case class MyError3(e: Throwable) extends RootError(e)

  test("Result using flatmap") {
    for {
      a <- Result.fromFuture(Future(1))
      b <- Result.fromFuture(Future(2))
      c <- Result.fromFuture(Future(3))
    } yield assertEquals(a + b + c, 6)
  }
  test("Result using flatmap with typed errors") {
    val a = for {
      a <- Result.fromFuture(Future(1))
      b <- Result.fromFuture(Future(2))
      _ <- Result.failed(MyError(new RuntimeException("test message")))
    } yield assertEquals(a + b, 3)
    a.catchSome {
      case _: RuntimeException => Result.succeed(assert(false))
      case _: MyError => Result.succeed(assert(true))
    }
  }
  test("Result using sequence") {
    def getResult(i: Int) = Result.fromFuture(Future(i))

    Result
      .sequence(Seq(1, 2, 3).map(getResult))
      .map(_.sum)
      .map(sum => assertEquals(sum, 6))
  }

  test("Result fail with typed error") {
    def getResult(i: Int) =
      if (i < 2)
        Result.succeed(i)
      else
        Result.failed(new IllegalArgumentException("test message"))

    Result
      .sequence(Seq(1, 2, 3).map(getResult))
      .catchSome {
        case _: IllegalArgumentException => Result.succeed(assert(true))
      }
  }

  test("Result using flatten") {
    val result1 = Result.fromFuture(Future(1))
    val result2 = Result.fromFuture(Future(result1))
    result2.flatten.catchSome {
      case _: IllegalArgumentException => Result.succeed(assert(true))
    }
  }

  test("Result apply works") {
    val result = Result(1)
    result.map(one => assertEquals(one, 1))
  }

  test("Result can fail using apply") {
    def failingFunc(): Unit = throw new RuntimeException("test message")

    val result = Result[Unit](failingFunc()).mapError(MyError)
    result.catchSome {
      case _: MyError => Result.succeed(assert(true))
    }
  }

  test("Result using zip") {
    val result1 = Result(1)
    val result2 = Result(2)
    result1.zip(result2).map { tuple =>
      assertEquals(tuple, (1, 2))
    }
  }

  test("Result catchAll") {
    val a = for {
      a <- Result.failed(MyError2(new IllegalArgumentException("Bad argument")))
      _ <- Result.failed(MyError(new RuntimeException("test message")))
    } yield assertEquals(a, -1)
    a.catchAll {
      _ => Result.succeed(assert(true))
    }
  }
}
