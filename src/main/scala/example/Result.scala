package example

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

class Result[+E <: Throwable, +A](future: Future[A]) {
  def toFuture: Future[A] = future

  // TODO: Fatal Error on exception in f
  // Another with A => Try[B] to catch error
  def map[B](f: A => B)(implicit ec: ExecutionContext): Result[E, B] =
    new Result(future.map(f))

  def flatMap[E2 >: E <: Throwable, B](f: A => Result[E2, B])(implicit ec: ExecutionContext): Result[E2, B] = {
    val newFuture: Future[B] = future.flatMap { a =>
      f(a).toFuture
    }
    new Result[E2, B](newFuture)
  }

  def onComplete(f: Try[A] => Unit)(implicit ec: ExecutionContext): Unit =
    future.onComplete(f)

}

object Result {


  final def fromFuture[A](body: Future[A])(implicit ec: ExecutionContext): Result[Throwable, A] =
    new Result(body)

  final def fromFuture[E <: Throwable, A](f: Throwable => E, body: Future[A])
                                         (implicit ec: ExecutionContext): Result[E, A] =
    new Result(body.recoverWith {
      case e if NonFatal(e) => Future.failed(f(e))
    })


  final def successful[A](value: A): Result[Nothing, A] =
    new Result(Future.successful(value))

  final def failed[E <: Throwable](exception: E): Result[E, Nothing] =
    new Result(Future.failed(exception))

  final def fromTry[A](body: Try[A]): Result[Throwable, A] =
    new Result(Future.fromTry(body))

  final def attempt[E <: Throwable, A](f: Throwable => E, body: A): Result[E, A] = {
    val future = Future.fromTry {
      Try(body)
        .fold(
          e => Failure(f(e)),
          a => Success(a)
        )
    }
    new Result(future)
  }

  final def sequence[E <: Throwable, A](results: Seq[Result[E, A]])
                                       (implicit ec: ExecutionContext): Result[E, Seq[A]] = 
    new Result(Future.sequence(results.map(_.toFuture)))

}

object Main extends App {

  import scala.concurrent.ExecutionContext.Implicits.global

  sealed abstract class RootError extends Throwable
  case class MyError(error: Throwable) extends RootError
  case class MyError2(error: Throwable) extends RootError

  private val result: Result[RootError, Unit] = for {
    a <- Result.fromFuture(MyError, Future.successful(1)) // Result[MyError, Int]
    b <- Result.fromFuture(MyError2, Future.successful(1)) // Result[MyError2, Int]
    c <- Result.successful(1) // Result[Nothing, Int]
  } yield {
    println(a + b + c)
  }

  result.onComplete {
    case Success(_) =>
      System.exit(0)
    case Failure(_) =>
      System.exit(1)
  }
  Await.ready(result.toFuture, Duration.apply("10s"))


}