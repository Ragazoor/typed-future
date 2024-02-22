package dev.ragz.result

import munit.FunSuite

import scala.concurrent.{ Future => StdFuture }
import scala.util.Try

class ResultSpec extends FunSuite {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def munitValueTransforms = super.munitValueTransforms ++ List(
    new ValueTransform(
      "TypedFuture",
      { case Result(future) =>
        future
      }
    )
  )

  abstract class RootError(e: Throwable) extends Exception(e)

  case class MyError(e: Throwable) extends RootError(e)

  case class MyError2(e: Throwable) extends RootError(e)

  case class MyError3(e: Throwable) extends RootError(e)

  test("Typed Future using flatmap") {
    for {
      a <- Result(1)
      b <- Result.fromFuture(StdFuture(2))
      c <- Result.fromTry(Try(3))
      d <- Result.fromEither(Right(4))
    } yield assertEquals(a + b + c + d, 10)
  }

  test("Typed Future using flatmap with typed errors") {
    val a = for {
      a <- Result(1)
      b <- Result(2)
      _ <- Result.failed(MyError(new RuntimeException("test message")))
    } yield assertEquals(a + b, 3)
    a.catchSome {
      case _: RuntimeException => Result.successful(assert(false))
      case _: MyError          => Result.successful(assert(true))
    }
  }

  test("Typed Future using sequence") {
    def getResult(i: Int) = Result(i)

    Result
      .sequence(Seq(1, 2, 3).map(getResult))
      .map(_.sum)
      .map(sum => assertEquals(sum, 6))
  }

  test("Typed Future fail with typed error") {
    def getResult(i: Int) =
      if (i < 2)
        Result.successful(i)
      else
        Result.failed(new IllegalArgumentException("test message"))

    Result
      .sequence(Seq(1, 2, 3).map(getResult))
      .catchSome { case _: IllegalArgumentException =>
        Result.successful(assert(true))
      }
  }

  test("Typed Future.fromEither fail with typed error") {
    Result.fromEither(Left(new RuntimeException("Test message"))).catchSome { case _: RuntimeException =>
      Result.successful(assert(true))
    }
  }

  test("Typed Future using flatten") {
    val result1 = Result(1)
    val result2 = Result(result1)
    result2.flatten.catchSome { case _: IllegalArgumentException =>
      Result.successful(assert(true))
    }
  }

  test("Typed Future apply works") {
    val result = Result(1)
    result.map(one => assertEquals(one, 1))
  }

  test("Typed Future can fail using apply") {
    def failingFunc(): Unit = throw new RuntimeException("test message")

    val result = Result[Unit](failingFunc()).mapError(MyError.apply)
    result.catchSome { case _: MyError =>
      Result.successful(assert(true))
    }
  }

  test("Typed Future using zip") {
    val result1 = Result(1)
    val result2 = Result(2)
    result1.zip(result2).map { tuple =>
      assertEquals(tuple, (1, 2))
    }
  }

  test("Typed Future catchAll") {
    val a = for {
      a <- Result.failed(MyError2(new IllegalArgumentException("Bad argument")))
      _ <- Result.failed(MyError(new RuntimeException("test message")))
    } yield assertEquals(a, -1)
    a.catchAll { _ =>
      Result.successful(assert(true))
    }
  }

  test("CatchSome does not catch errors not specified") {
    Result
      .failed(new RuntimeException("Test message"))
      .catchSome { case _: IllegalArgumentException =>
        Result.successful(assert(false))
      }
      .catchSome { case _: RuntimeException =>
        Result.successful(assert(true))
      }
  }

  test("Typed Future cannot catch fatal errors") {
    val a = for {
      _ <- Result.fatal(MyError(new IllegalArgumentException("Bad argument")))
    } yield assert(false)
    a.catchAll { _ =>
      Result.successful(assert(false))
    }.toFuture.recover { case _: FatalError =>
      assert(true)
    }
  }

  test("Future.failed returns error") {
    Result
      .failed(new IllegalArgumentException("Bad argument"))
      .failed
      .map(e => assertEquals(e.getMessage, "Bad argument"))
  }

  test("Future.failed fails with NoSuchElementException if it is a success") {
    Result
      .successful(1)
      .failed
      .map(_ => assert(false))
      .catchSome { case _: NoSuchElementException =>
        Result.successful(assert(true))
      }
  }

  test("Future.failed fails with FatalError if it contains a FatalError") {
    Result
      .fatal(new RuntimeException("Test message"))
      .failed
      .map(_ => assert(false))
      .catchSome { case _: NoSuchElementException =>
        Result.successful(assert(false))
      }
      .toFuture
      .recover { case _: FatalError =>
        assert(true)
      }
  }
}
