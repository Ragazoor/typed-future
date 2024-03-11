
import munit.FunSuite

import scala.concurrent.{ Future => StdFuture }
import scala.util.Try

class IOSpec extends FunSuite {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def munitValueTransforms = super.munitValueTransforms ++ List(
    new ValueTransform(
      "TypedFuture",
      { case IO(future) =>
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
      a <- IO(1)
      b <- IO.fromFuture(StdFuture(2))
      c <- IO.fromTry(Try(3))
      d <- IO.fromEither(Right(4))
    } yield assertEquals(a + b + c + d, 10)
  }

  test("Typed Future using flatmap with typed errors") {
    val a = for {
      a <- IO(1)
      b <- IO(2)
      _ <- IO.failed(MyError(new RuntimeException("test message")))
    } yield assertEquals(a + b, 3)
    a.catchSome {
      case _: RuntimeException => IO.successful(assert(false))
      case _: MyError          => IO.successful(assert(true))
    }
  }

  test("Typed Future using sequence") {
    def getResult(i: Int) = IO(i)

    IO
      .sequence(Seq(1, 2, 3).map(getResult))
      .map(_.sum)
      .map(sum => assertEquals(sum, 6))
  }

  test("Typed Future fail with typed error") {
    def getResult(i: Int) =
      if (i < 2)
        IO.successful(i)
      else
        IO.failed(new IllegalArgumentException("test message"))

    IO
      .sequence(Seq(1, 2, 3).map(getResult))
      .catchSome { case _: IllegalArgumentException =>
        IO.successful(assert(true))
      }
  }

  test("Typed Future.fromEither fail with typed error") {
    IO.fromEither(Left(new RuntimeException("Test message"))).catchSome { case _: RuntimeException =>
      IO.successful(assert(true))
    }
  }

  test("Typed Future using flatten") {
    val result1 = IO(1)
    val result2 = IO(result1)
    result2.flatten.catchSome { case _: IllegalArgumentException =>
      IO.successful(assert(true))
    }
  }

  test("Typed Future apply works") {
    val result = IO(1)
    result.map(one => assertEquals(one, 1))
  }

  test("Typed Future can fail using apply") {
    def failingFunc(): Unit = throw new RuntimeException("test message")

    val result = IO[Unit](failingFunc()).mapError(MyError.apply)
    result.catchSome { case _: MyError =>
      IO.successful(assert(true))
    }
  }

  test("Typed Future using zip") {
    val result1 = IO(1)
    val result2 = IO(2)
    result1.zip(result2).map { tuple =>
      assertEquals(tuple, (1, 2))
    }
  }

  test("Typed Future catchAll") {
    val a = for {
      a <- IO.failed(MyError2(new IllegalArgumentException("Bad argument")))
      _ <- IO.failed(MyError(new RuntimeException("test message")))
    } yield assertEquals(a, -1)
    a.catchAll { _ =>
      IO.successful(assert(true))
    }
  }

  test("CatchSome does not catch errors not specified") {
    IO
      .failed(new RuntimeException("Test message"))
      .catchSome { case _: IllegalArgumentException =>
        IO.successful(assert(false))
      }
      .catchSome { case _: RuntimeException =>
        IO.successful(assert(true))
      }
  }

  test("Typed Future cannot catch fatal errors") {
    val a = for {
      _ <- IO.fatal(MyError(new IllegalArgumentException("Bad argument")))
    } yield assert(false)
    a.catchAll { _ =>
      IO.successful(assert(false))
    }.toFuture.recover { case _: FatalError =>
      assert(true)
    }
  }

  test("Future.failed returns error") {
    IO
      .failed(new IllegalArgumentException("Bad argument"))
      .failed
      .map(e => assertEquals(e.getMessage, "Bad argument"))
  }

  test("Future.failed fails with NoSuchElementException if it is a success") {
    IO
      .successful(1)
      .failed
      .map(_ => assert(false))
      .catchSome { case _: NoSuchElementException =>
        IO.successful(assert(true))
      }
  }

  test("Future.failed fails with FatalError if it contains a FatalError") {
    IO
      .fatal(new RuntimeException("Test message"))
      .failed
      .map(_ => assert(false))
      .catchSome { case _: NoSuchElementException =>
        IO.successful(assert(false))
      }
      .toFuture
      .recover { case _: FatalError =>
        assert(true)
      }
  }

  test("Future.mapEither") {
    IO.successful(1)
      .mapEither(_ => Right(2))
      .map(result => assert(result == 2))
  }

  test("Future.mapEither, with typed error") {
    IO.successful(1)
      .mapEither[RuntimeException, Int](_ => Left(new RuntimeException("Test message")))
      .map(_ => assert(false))
      .catchSome { case _: RuntimeException =>
        IO.successful(assert(true))
      }
  }

  test("Future.mapTry") {
    IO.successful(1)
      .mapTry(_ => Try(2))
      .map(result => assert(result == 2))
  }

}
