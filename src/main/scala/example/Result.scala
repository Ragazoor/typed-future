package example

import scala.concurrent.ExecutionContext.parasitic
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

  def flatten[E2 >: E <: Throwable, B](implicit ev: A <:< Result[E2, B]): Result[E2, B] =
    flatMap(ev)(parasitic)

  def onComplete(f: Try[A] => Unit)(implicit ec: ExecutionContext): Unit =
    future.onComplete(f)

  def filter(p: A => Boolean)(implicit executor: ExecutionContext): Result[E, A] =
    new Result(future.filter(p))

  final def withFilter(p: A => Boolean)(implicit executor: ExecutionContext): Result[E, A] =
    filter(p)(executor)

  def collect[B](pf: PartialFunction[A, B])(implicit executor: ExecutionContext): Result[E, B] =
    new Result(future.collect(pf))

  def zip[E2 >: E <: Throwable, B](that: Result[E2, B]): Result[E2, (A, B)] =
    zipWith(that)(Result.zipWithTuple2Fun)(parasitic)

  def zipWith[E2 >: E <: Throwable, U, R](that: Result[E2, U])(f: (A, U) => R)
                                         (implicit executor: ExecutionContext): Result[E2, R] =
    new Result(future.zipWith(that.toFuture)(f))

}

object Result {
  private final val _zipWithTuple2: (Any, Any) => (Any, Any) = Tuple2.apply _

  private[example] final def zipWithTuple2Fun[T, U]: (T, U) => (T, U) =
    _zipWithTuple2.asInstanceOf[(T, U) => (T, U)]


  final def fromFuture[A](body: Future[A])(implicit ec: ExecutionContext): Result[Throwable, A] =
    new Result(body)

  final def fromFuture[E <: Throwable, A](f: Throwable => E, body: Future[A])
                                         (implicit ec: ExecutionContext): Result[E, A] =
    new Result(body.recoverWith {
      case e if NonFatal(e) => Future.failed(f(e))
    })


  final def successful[A](value: A): Result[Nothing, A] =
    new Result(Future.successful(value))

  final def sequence[E <: Throwable, A](results: Seq[Result[E, A]])
                                       (implicit ec: ExecutionContext): Result[E, Seq[A]] = {
    val future = Future.sequence(results.map(_.toFuture))
    new Result(future)
  }

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
