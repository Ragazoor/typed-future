package io.github.ragazoor

import org.openjdk.jmh.annotations._

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future => StdFuture }
import scala.util.{ Success, Try }

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.Throughput))
@Threads(value = 1)
class IOBenchmark {
  private val size      = 1_000_000
  private val recursion = 100_000
  private val input     = 1 to size

  protected final def await[T](fut: StdFuture[T]): Boolean = {
    var r: Option[Try[T]] = None
    while (r eq None) r = fut.value
    r.get.isInstanceOf[Success[T]]
  }

  protected final def await[E <: Throwable, T](result: Attempt[E, T]): Boolean = {
    var r: Option[Try[T]] = None
    while (r eq None) r = result.value
    r.get.isInstanceOf[Success[T]]
  }

  @Benchmark def futureSequence: Boolean =
    await(StdFuture.sequence(input.map(StdFuture.successful)))

  @Benchmark def resultSequence: Boolean =
    await(Attempt.sequence(input.map(Attempt.successful)))

  @tailrec private[this] final def futureFlatMapRec(i: Int, f: StdFuture[Int])(implicit
    ec: ExecutionContext
  ): StdFuture[Int] =
    if (i > 0) futureFlatMapRec(i - 1, f.flatMap(StdFuture.successful)(ec))(ec)
    else f

  @Benchmark final def futureFlatMap: Boolean =
    await(futureFlatMapRec(recursion, StdFuture.successful(1)))

  @tailrec private[this] final def resultFlatMapRec(i: Int, f: Attempt[Nothing, Int])(implicit
    ec: ExecutionContext
  ): Attempt[Nothing, Int] =
    if (i > 0) resultFlatMapRec(i - 1, f.flatMap(Attempt.successful)(ec))(ec)
    else f

  @Benchmark final def resultFlatMap: Boolean =
    await(resultFlatMapRec(recursion, Attempt.successful(1)))

  @tailrec private[this] final def futureMapRec(i: Int, f: StdFuture[Int])(implicit
    ec: ExecutionContext
  ): StdFuture[Int] =
    if (i > 0) futureMapRec(i - 1, f.map(identity)(ec))(ec)
    else f

  @Benchmark final def futureMap: Boolean =
    await(futureMapRec(recursion, StdFuture.successful(1)))

  @tailrec private[this] final def resultMapRec(i: Int, f: Attempt[Nothing, Int])(implicit
    ec: ExecutionContext
  ): Attempt[Nothing, Int] =
    if (i > 0) resultMapRec(i - 1, f.map(identity)(ec))(ec)
    else f

  @Benchmark final def resultMap: Boolean =
    await(resultMapRec(recursion, Attempt.successful(1)))

  @tailrec private[this] final def futureRecoverWithRec(i: Int, f: StdFuture[Int])(implicit
    ec: ExecutionContext
  ): StdFuture[Int] =
    if (i > 0) futureRecoverWithRec(i - 1, f.recoverWith(e => StdFuture.failed(e))(ec))(ec)
    else f

  @tailrec private[this] final def resultMapErrorRec(i: Int, f: Attempt[RuntimeException, Int])(implicit
    ec: ExecutionContext
  ): Attempt[RuntimeException, Int] =
    if (i > 0) resultMapErrorRec(i - 1, f.mapError(identity)(ec))(ec)
    else f

  @Benchmark final def futureRecover: Boolean =
    await(futureRecoverWithRec(recursion, StdFuture.failed[Int](new RuntimeException("Future error"))))

  @Benchmark final def resultMapError: Boolean =
    await(resultMapErrorRec(recursion, Attempt.failed(new RuntimeException("Result error"))))
}
