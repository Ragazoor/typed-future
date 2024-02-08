package dev.ragz.result

import org.openjdk.jmh.annotations._

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Success, Try }

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@Threads(value = 1)
class ResultBenchmark {
  private val size        = 1_000_000
  protected val recursion = 100_000
  protected val input     = 1 to size

  protected final def await[T](fut: Future[T]): Boolean = {
    var r: Option[Try[T]] = None
    do r = fut.value while (r eq None);
    r.get.isInstanceOf[Success[T]]
  }

  protected final def await[E <: Throwable, T](result: Result[E, T]): Boolean = {
    var r: Option[Try[T]] = None
    do r = result.value while (r eq None);
    r.get.isInstanceOf[Success[T]]
  }

  @Benchmark def futureSequence: Boolean =
    await(Future.sequence(input.map(Future.successful)))

  @Benchmark def resultSequence: Boolean =
    await(Result.sequence(input.map(Result.succeed)))

  @tailrec private[this] final def futureFlatMapRec(i: Int, f: Future[Int])(implicit
    ec: ExecutionContext
  ): Future[Int] =
    if (i > 0) futureFlatMapRec(i - 1, f.flatMap(Future.successful)(ec))(ec)
    else f

  @Benchmark final def futureFlatMap: Boolean =
    await(futureFlatMapRec(recursion, Future.successful(1)))

  @tailrec private[this] final def resultFlatMapRec(i: Int, f: Result[Nothing, Int])(implicit
    ec: ExecutionContext
  ): Result[Nothing, Int] =
    if (i > 0) resultFlatMapRec(i - 1, f.flatMap(Result.succeed)(ec))(ec)
    else f

  @Benchmark final def resultFlatMap: Boolean =
    await(resultFlatMapRec(recursion, Result.succeed(1)))

  @tailrec private[this] final def futureMapRec(i: Int, f: Future[Int])(implicit ec: ExecutionContext): Future[Int] =
    if (i > 0) futureMapRec(i - 1, f.map(identity)(ec))(ec)
    else f

  @Benchmark final def futureMap: Boolean =
    await(futureMapRec(recursion, Future.successful(1)))

  @tailrec private[this] final def resultMapRec(i: Int, f: Result[Nothing, Int])(implicit
    ec: ExecutionContext
  ): Result[Nothing, Int] =
    if (i > 0) resultMapRec(i - 1, f.map(identity)(ec))(ec)
    else f

  @Benchmark final def resultMap: Boolean =
    await(resultMapRec(recursion, Result.succeed(1)))

  @tailrec private[this] final def futureRecoverWithRec(i: Int, f: Future[Int])(implicit
    ec: ExecutionContext
  ): Future[Int] =
    if (i > 0) futureRecoverWithRec(i - 1, f.recoverWith(e => Future.failed(e))(ec))(ec)
    else f

  @tailrec private[this] final def resultMapErrorRec(i: Int, f: Result[RuntimeException, Int])(implicit
    ec: ExecutionContext
  ): Result[RuntimeException, Int] =
    if (i > 0) resultMapErrorRec(i - 1, f.mapError(identity)(ec))(ec)
    else f

  @Benchmark final def futureRecover: Boolean =
    await(futureRecoverWithRec(recursion, Future.failed[Int](new RuntimeException("Future error"))))

  @Benchmark final def resultMapError: Boolean =
    await(resultMapErrorRec(recursion, Result.fail(new RuntimeException("Result error"))))
}
