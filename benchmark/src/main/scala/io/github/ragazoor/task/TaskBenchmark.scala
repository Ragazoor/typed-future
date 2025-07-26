package io.github.ragazoor.task

import org.openjdk.jmh.annotations._

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future => StdFuture}
import scala.util.{Success, Try}

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.Throughput))
@Threads(value = 1)
class TaskBenchmark {
  private val size      = 1_000_000
  private val recursion = 100_000
  private val input     = 1 to size

  protected final def await[T](fut: StdFuture[T]): Boolean = {
    var r: Option[Try[T]] = None
    while (r eq None) r = fut.value
    r.get.isInstanceOf[Success[T]]
  }

  protected final def await[E <: Throwable, T](task: Task[E, T]): Boolean = {
    var r: Option[Try[T]] = None
    while (r eq None) r = task.value
    r.get.isInstanceOf[Success[T]]
  }

  @Benchmark def futureSequence: Boolean =
    await(StdFuture.sequence(input.map(StdFuture.successful)))

  @Benchmark def taskSequence: Boolean =
    await(Task.sequence(input.map(Task.successful)))

  @tailrec private[this] final def futureFlatMapRec(i: Int, f: StdFuture[Int])(implicit
    ec: ExecutionContext
  ): StdFuture[Int] =
    if (i > 0) futureFlatMapRec(i - 1, f.flatMap(StdFuture.successful)(ec))(ec)
    else f

  @Benchmark final def futureFlatMap: Boolean =
    await(futureFlatMapRec(recursion, StdFuture.successful(1)))

  @tailrec private[this] final def taskFlatMapRec(i: Int, f: Task[Nothing, Int])(implicit
    ec: ExecutionContext
  ): Task[Nothing, Int] =
    if (i > 0) taskFlatMapRec(i - 1, f.flatMap(Task.successful)(ec))(ec)
    else f

  @Benchmark final def taskFlatMap: Boolean =
    await(taskFlatMapRec(recursion, Task.successful(1)))

  @tailrec private[this] final def futureMapRec(i: Int, f: StdFuture[Int])(implicit
    ec: ExecutionContext
  ): StdFuture[Int] =
    if (i > 0) futureMapRec(i - 1, f.map(identity)(ec))(ec)
    else f

  @Benchmark final def futureMap: Boolean =
    await(futureMapRec(recursion, StdFuture.successful(1)))

  @tailrec private[this] final def taskMapRec(i: Int, f: Task[Nothing, Int])(implicit
    ec: ExecutionContext
  ): Task[Nothing, Int] =
    if (i > 0) taskMapRec(i - 1, f.map(identity)(ec))(ec)
    else f

  @Benchmark final def taskMap: Boolean =
    await(taskMapRec(recursion, Task.successful(1)))

  @tailrec private[this] final def futureRecoverWithRec(i: Int, f: StdFuture[Int])(implicit
    ec: ExecutionContext
  ): StdFuture[Int] =
    if (i > 0) futureRecoverWithRec(i - 1, f.recoverWith(e => StdFuture.failed(e))(ec))(ec)
    else f

  @tailrec private[this] final def taskMapErrorRec(i: Int, f: Task[RuntimeException, Int])(implicit
    ec: ExecutionContext
  ): Task[RuntimeException, Int] =
    if (i > 0) taskMapErrorRec(i - 1, f.mapError(identity)(ec))(ec)
    else f

  @Benchmark final def futureRecover: Boolean =
    await(futureRecoverWithRec(recursion, StdFuture.failed[Int](new RuntimeException("Future error"))))

  @Benchmark final def taskMapError: Boolean =
    await(taskMapErrorRec(recursion, Task.failed(new RuntimeException("Task error"))))
}
