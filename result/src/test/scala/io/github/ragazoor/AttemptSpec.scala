package io.github.ragazoor

import munit.FunSuite

import scala.concurrent.{ Future => StdFuture }
import scala.util.Try

class AttemptSpec extends FunSuite {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def munitValueTransforms = super.munitValueTransforms ++ List(
    new ValueTransform(
      "Attempt",
      { case Attempt(future, _) => future }
    )
  )

  abstract class RootError(e: Throwable) extends Exception(e)

  case class MyError(e: Throwable) extends RootError(e)

  case class MyError2(e: Throwable) extends RootError(e)

  case class MyError3(e: Throwable) extends RootError(e)

  test("Typed Future using flatmap") {
    for {
      a <- Attempt(1)
      b <- Attempt.fromFuture(StdFuture(2))
      c <- Attempt.fromTry(Try(3))
      d <- Attempt.fromEither(Right(4))
    } yield assertEquals(a + b + c + d, 10)
  }

  test("Typed Future using flatmap with typed errors") {
    val a = for {
      a <- Attempt(1)
      b <- Attempt(2)
      _ <- Attempt.failed(MyError(new RuntimeException("test message")))
    } yield assertEquals(a + b, 3)
    a.catchSome {
      case _: RuntimeException => Attempt.successful(assert(false))
      case _: MyError          => Attempt.successful(assert(true))
    }
  }

  test("Typed Future using sequence") {
    def getResult(i: Int) = Attempt(i)

    Attempt
      .sequence(Seq(1, 2, 3).map(getResult))
      .map(_.sum)
      .map(sum => assertEquals(sum, 6))
  }

  test("Typed Future fail with typed error") {
    def getResult(i: Int) =
      if (i < 2)
        Attempt.successful(i)
      else
        Attempt.failed(new IllegalArgumentException("test message"))

    Attempt
      .sequence(Seq(1, 2, 3).map(getResult))
      .catchSome { case _: IllegalArgumentException =>
        Attempt.successful(assert(true))
      }
  }

  test("Typed Future.fromEither fail with typed error") {
    Attempt.fromEither(Left(new RuntimeException("Test message"))).catchSome { case _: RuntimeException =>
      Attempt.successful(assert(true))
    }
  }

  test("Typed Future using flatten") {
    val result1 = Attempt(1)
    val result2 = Attempt(result1)
    result2.flatten.catchSome { case _: IllegalArgumentException =>
      Attempt.successful(assert(true))
    }
  }

  test("Typed Future apply works") {
    val result = Attempt(1)
    result.map(one => assertEquals(one, 1))
  }

  test("Typed Future can fail using apply") {
    def failingFunc(): Unit = throw new RuntimeException("test message")

    val result = Attempt[Unit](failingFunc()).mapError(MyError.apply)
    result.catchSome { case _: MyError =>
      Attempt.successful(assert(true))
    }
  }

  test("Typed Future using zip") {
    val result1 = Attempt(1)
    val result2 = Attempt(2)
    result1.zip(result2).map { tuple =>
      assertEquals(tuple, (1, 2))
    }
  }

  test("Typed Future catchAll") {
    val a = for {
      a <- Attempt.failed(MyError2(new IllegalArgumentException("Bad argument")))
      _ <- Attempt.failed(MyError(new RuntimeException("test message")))
    } yield assertEquals(a, -1)
    a.catchAll { _ =>
      Attempt.successful(assert(true))
    }
  }

  test("CatchSome does not catch errors not specified") {
    Attempt
      .failed(new RuntimeException("Test message"))
      .catchSome { case _: IllegalArgumentException =>
        Attempt.successful(assert(false))
      }
      .catchSome { case _: RuntimeException =>
        Attempt.successful(assert(true))
      }
  }

  test("Typed Future cannot catch fatal errors") {
    val fatalIO = for {
      _ <- Attempt.fatal(new RuntimeException("Killing the process"))
    } yield assert(false)
    val isFatal = fatalIO.isFatal
    fatalIO
      .catchAll(_ => Attempt.successful(assert(false)))
      .toFuture
      .recover { case _: RuntimeException =>
        assert(isFatal)
      }
  }

  test("Future.failed returns error") {
    Attempt
      .failed(new IllegalArgumentException("Bad argument"))
      .failed
      .map(e => assertEquals(e.getMessage, "Bad argument"))
  }

  test("Future.failed fails with NoSuchElementException if it is a success") {
    Attempt
      .successful(1)
      .failed
      .map(_ => assert(false))
      .catchSome { case _: NoSuchElementException =>
        Attempt.successful(assert(true))
      }
  }

  test("Future.failed fails with FatalError if it contains a FatalError") {
    val fatalIO = Attempt
      .fatal(new RuntimeException("Test message"))
      .failed
      .map(_ => assert(false))
      .catchSome { case _: NoSuchElementException =>
        Attempt.successful(assert(false))
      }
    val isFatal = fatalIO.isFatal
    fatalIO.toFuture.recover { case _: RuntimeException =>
      assert(isFatal)
    }
  }

  test("Future.mapEither") {
    Attempt
      .successful(1)
      .mapEither(_ => Right(2))
      .map(result => assert(result == 2))
  }

  test("Future.mapEither, with typed error") {
    Attempt
      .successful(1)
      .mapEither[RuntimeException, Int](_ => Left(new RuntimeException("Test message")))
      .map(_ => assert(false))
      .catchSome { case _: RuntimeException =>
        Attempt.successful(assert(true))
      }
  }

  test("Future.mapTry") {
    Attempt
      .successful(1)
      .mapTry(_ => Try(2))
      .map(result => assert(result == 2))
  }

  test("IO mapError cannot catch fatal errors") {
    val result = for {
      _ <- Attempt.fatal(new RuntimeException("Test message"))
    } yield assert(false)

    result
      .mapError(MyError.apply)
      .catchSome { case _: MyError =>
        Attempt.successful(assert(false))
      }
      .toFuture
      .recover {
        case _: RuntimeException => assert(true)
        case e                   => assert(e.getMessage == "Test message")
      }

  }
}
