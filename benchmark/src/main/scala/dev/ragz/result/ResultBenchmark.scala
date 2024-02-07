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
  private val size      = 1_000_000
  private val recursion = 10_000
  private val input     = 1 to size

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

  @tailrec private[this] final def nextS(i: Int, f: Future[Int])(implicit ec: ExecutionContext): Future[Int] =
    if (i > 0) nextS(i - 1, f.flatMap(Future.successful)(ec))(ec)
    else f

  @Benchmark final def futureFlatMap: Boolean =
    await(nextS(recursion, Future.successful(1)))

  @tailrec private[this] final def nextS(i: Int, f: Result[Nothing, Int])(implicit
    ec: ExecutionContext
  ): Result[Nothing, Int] =
    if (i > 0) nextS(i - 1, f.flatMap(Result.succeed)(ec))(ec)
    else f

  @Benchmark final def resultFlatMap: Boolean =
    await(nextS(recursion, Result.succeed(1)))

}
