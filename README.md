# A Future with typed errors
A Future based monad with typed errors.
Designed to be a replacement for the `scala.concurrent.Future` with minimal
migration needed. Entirely built on top of the `scala.concurrent.Future` it has
the same performance and easily integrates into existing `Future` based libraries.

# Installation

Setup via `build.sbt`:

```sbt
libraryDependencies += "io.github.ragazoor" %% "io" % "0.1.0"
```

# Getting Started
Compile and or run test

```shell
sbt compile
```

```shell
sbt test
```
## Examples
```scala
import dev.ragz.io.implicits._

val successIO: IO[Nothing, Int] = IO.successful(a + 1)
val failedIO: IO[Throwable, Nothing] = IO.failed(new Exception("error"))
val typedFailureIO: IO[RunTimeException, Nothing] = IO.failed(new RuntimeException("error"))
val TaskIO: IO[Throwable, Int] = IO(1)
val standardFuture: scala.concurrent.Future[Int] = successIO.toFuture
val ioFromFuture: IO[Throwable, Int] = standardFuture.io
```
This is the basics of using `IO` in your code. The IO has the same API
as the `scala.concurrent.Future`, and thanks to the type alias
`type Future[+A] = IO[Throwable, A]` we don't need to rename `Future`s 
all over the code base.
## Error handling

```scala 
val throwableIO: IO[Throwable, Nothing] = IO.failed(new Exception("error"))
val runTimeExceptinIO: IO[RunTimeException, Nothing] =
  throwableIO.mapError {
    case e: Throwable => new RunTimeException(e)
  }

```

## Migration
The goal is to eventually be able to replace `scala.concurrent`, however we
not everything is available yet. If you are only using `Future`, 
`ExecutionContext` and `NonFatal` you can use the following to migrate 
most of the code:
```text
replace: 
import.scala.concurrent.*

with: 
import io.github.ragazoor.*
import io.github.ragazoor.implicits.*
import io.github.ragazoor.migration.implicits.*
```

There are a few occurrences where we need to manually fix the code:
- If we are using a third-party library returning a `scala.concurrent.Future` 
 we need to convert it to `IO` using `.io` and the implicit in 
 `ragazoor.implicits.*`. 
- If there are async tests using `scala.concurrent.Future` but does not
have `scala.concurrent` in imported we need to add 
`import io.github.ragazoor.migration.implicits.*`.
- If you are using implicit classes that are extending 
`scala.concurrent.Future` the compiler will not be able to convert 
like one might think using the migration implicits. So we need to make 
it explicit:
```scala
implicit class MyImplicitClassFunction(f: Future[Int]) {
  def bar: Future[Option[Int]] = f.map(Some(_))
}
def foo: IO[Throwable, Int] = ???
val a: IO[Throwable, Option[Int]] = foo.myImplicitClassFunction.io // does not compile
val a: IO[Throwable, Option[Int]] = foo.toFuture.myImplicitClassFunction.io // compiles
```

## Benchmarks
Any contribution to more or improved benchmarks are most welcome! 

Run benchmarks
```shell
sbt "benchmark/jmh:run -i 10 -wi 10 -f 1 -t 1 dev.ragz.future.FutureBenchmark"
```

Example benchmark
```text
[info] Benchmark                        Mode  Cnt   Score   Error  Units
[info] FutureBenchmark.futureFlatMap   thrpt   10  24.116 ± 0.078  ops/s
[info] FutureBenchmark.futureMap       thrpt   10  27.629 ± 0.490  ops/s
[info] FutureBenchmark.futureRecover   thrpt   10  24.488 ± 0.415  ops/s
[info] FutureBenchmark.futureSequence  thrpt   10   2.004 ± 0.203  ops/s
[info] FutureBenchmark.ioFlatMap       thrpt   10  22.395 ± 0.375  ops/s
[info] FutureBenchmark.ioMap           thrpt   10  27.328 ± 0.455  ops/s
[info] FutureBenchmark.ioMapError      thrpt   10  27.177 ± 0.041  ops/s
[info] FutureBenchmark.ioSequence      thrpt   10   1.817 ± 0.029  ops/s
[success] Total time: 1623 s (27:03), completed Feb 20, 2024, 7:02:20 PM
```
