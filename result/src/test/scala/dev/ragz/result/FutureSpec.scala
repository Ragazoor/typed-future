package dev.ragz.result

import munit.FunSuite

import scala.concurrent.{ Future => StdFuture }
import scala.util.Try

class FutureSpec extends FunSuite {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def munitValueTransforms = super.munitValueTransforms ++ List(
    new ValueTransform(
      "TypedFuture",
      { case Future(future) =>
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
      a <- Future(1)
      b <- Future.fromFuture(StdFuture(2))
      c <- Future.fromTry(Try(3))
      d <- Future.fromEither(Right(4))
    } yield assertEquals(a + b + c + d, 10)
  }

  test("Typed Future using flatmap with typed errors") {
    val a = for {
      a <- Future(1)
      b <- Future(2)
      _ <- Future.failed(MyError(new RuntimeException("test message")))
    } yield assertEquals(a + b, 3)
    a.catchSome {
      case _: RuntimeException => Future.successful(assert(false))
      case _: MyError          => Future.successful(assert(true))
    }
  }

  test("Typed Future using sequence") {
    def getResult(i: Int) = Future(i)

    Future
      .sequence(Seq(1, 2, 3).map(getResult))
      .map(_.sum)
      .map(sum => assertEquals(sum, 6))
  }

  test("Typed Future fail with typed error") {
    def getResult(i: Int) =
      if (i < 2)
        Future.successful(i)
      else
        Future.failed(new IllegalArgumentException("test message"))

    Future
      .sequence(Seq(1, 2, 3).map(getResult))
      .catchSome { case _: IllegalArgumentException =>
        Future.successful(assert(true))
      }
  }

  test("Typed Future.fromEither fail with typed error") {
    Future.fromEither(Left(new RuntimeException("Test message"))).catchSome { case _: RuntimeException =>
      Future.successful(assert(true))
    }
  }

  test("Typed Future using flatten") {
    val result1 = Future(1)
    val result2 = Future(result1)
    result2.flatten.catchSome { case _: IllegalArgumentException =>
      Future.successful(assert(true))
    }
  }

  test("Typed Future apply works") {
    val result = Future(1)
    result.map(one => assertEquals(one, 1))
  }

  test("Typed Future can fail using apply") {
    def failingFunc(): Unit = throw new RuntimeException("test message")

    val result = Future[Unit](failingFunc()).mapError(MyError.apply)
    result.catchSome { case _: MyError =>
      Future.successful(assert(true))
    }
  }

  test("Typed Future using zip") {
    val result1 = Future(1)
    val result2 = Future(2)
    result1.zip(result2).map { tuple =>
      assertEquals(tuple, (1, 2))
    }
  }

  test("Typed Future catchAll") {
    val a = for {
      a <- Future.failed(MyError2(new IllegalArgumentException("Bad argument")))
      _ <- Future.failed(MyError(new RuntimeException("test message")))
    } yield assertEquals(a, -1)
    a.catchAll { _ =>
      Future.successful(assert(true))
    }
  }

  test("CatchSome does not catch errors not specified") {
    Future
      .failed(new RuntimeException("Test message"))
      .catchSome { case _: IllegalArgumentException =>
        Future.successful(assert(false))
      }
      .catchSome { case _: RuntimeException =>
        Future.successful(assert(true))
      }
  }

  test("Typed Future cannot catch fatal errors") {
    val a = for {
      _ <- Future.fatal(MyError(new IllegalArgumentException("Bad argument")))
    } yield assert(false)
    a.catchAll { _ =>
      Future.successful(assert(false))
    }.toFuture.recover { case _: FatalError =>
      assert(true)
    }
  }
}
