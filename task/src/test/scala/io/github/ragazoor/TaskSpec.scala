package io.github.ragazoor

import munit.FunSuite

import scala.concurrent.{ Future => StdFuture }
import scala.util.Try

class TaskSpec extends FunSuite {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def munitValueTransforms = super.munitValueTransforms ++ List(
    new ValueTransform(
      "Attempt",
      { case Task(future) => future }
    )
  )

  abstract class RootError(e: Throwable) extends Exception(e)

  case class MyError(e: Throwable) extends RootError(e)

  case class MyError2(e: Throwable) extends RootError(e)

  case class MyError3(e: Throwable) extends RootError(e)

  test("Task using flatmap") {
    for {
      a <- Task(1)
      b <- Task.fromFuture(StdFuture(2))
      c <- Task.fromTry(Try(3))
      d <- Task.fromEither(Right(4))
    } yield assertEquals(a + b + c + d, 10)
  }

  test("Task using flatmap with typed errors") {
    val a = for {
      a <- Task(1)
      b <- Task(2)
      _ <- Task.failed(MyError(new RuntimeException("test message")))
    } yield assertEquals(a + b, 3)
    a.catchSome {
      case _: RuntimeException => Task.successful(assert(false))
      case _: MyError          => Task.successful(assert(true))
    }
  }

  test("Task using sequence") {
    def getResult(i: Int) = Task(i)

    Task
      .sequence(Seq(1, 2, 3).map(getResult))
      .map(_.sum)
      .map(sum => assertEquals(sum, 6))
  }

  test("Task fail with typed error") {
    def getResult(i: Int) =
      if (i < 2)
        Task.successful(i)
      else
        Task.failed(new IllegalArgumentException("test message"))

    Task
      .sequence(Seq(1, 2, 3).map(getResult))
      .catchSome { case _: IllegalArgumentException =>
        Task.successful(assert(true))
      }
  }

  test("Task.fromEither fail with typed error") {
    Task.fromEither(Left(new RuntimeException("Test message"))).catchSome { case _: RuntimeException =>
      Task.successful(assert(true))
    }
  }

  test("Task using flatten") {
    val result1 = Task(1)
    val result2 = Task(result1)
    result2.flatten.catchSome { case _: IllegalArgumentException =>
      Task.successful(assert(true))
    }
  }

  test("Task apply works") {
    val result = Task(1)
    result.map(one => assertEquals(one, 1))
  }

  test("Task can fail using apply") {
    def failingFunc(): Unit = throw new RuntimeException("test message")

    val result = Task[Unit](failingFunc()).mapError(MyError.apply)
    result.catchSome { case _: MyError =>
      Task.successful(assert(true))
    }
  }

  test("Task using zip") {
    val result1 = Task(1)
    val result2 = Task(2)
    result1.zip(result2).map { tuple =>
      assertEquals(tuple, (1, 2))
    }
  }

  test("Task catchAll") {
    val a = for {
      _ <- Task.failed(MyError2(new IllegalArgumentException("Bad argument")))
      _ <- Task.failed(MyError(new RuntimeException("test message")))
    } yield assert(false)
    a.catchAll { _ =>
      Task.successful(assert(true))
    }
  }

  test("CatchSome does not catch errors not specified") {
    Task
      .failed(new RuntimeException("Test message"))
      .catchSome { case _: IllegalArgumentException =>
        Task.successful(assert(false))
      }
      .catchSome { case _: RuntimeException =>
        Task.successful(assert(true))
      }
  }

  test("Task.failed returns error") {
    Task
      .failed(new IllegalArgumentException("Bad argument"))
      .failed
      .map(e => assertEquals(e.getMessage, "Bad argument"))
  }

  test("Task.failed fails with NoSuchElementException if it is a success") {
    Task
      .successful(1)
      .failed
      .map(_ => assert(false))
      .catchSome { case _: NoSuchElementException =>
        Task.successful(assert(true))
      }
  }

  test("Task.mapEither") {
    Task
      .successful(1)
      .mapEither(_ => Right(2))
      .map(result => assert(result == 2))
  }

  test("Task.mapEither, with typed error") {
    Task
      .successful(1)
      .mapEither[RuntimeException, Int](_ => Left(new RuntimeException("Test message")))
      .map(_ => assert(false))
      .catchSome { case _: RuntimeException =>
        Task.successful(assert(true))
      }
  }

  test("Task.mapTry") {
    Task
      .successful(1)
      .mapTry(_ => Try(2))
      .map(result => assert(result == 2))
  }

}
