# Scala IO
A Future based monad with typed errors.
Designed to be a drop in replacement for `Future`, 
built entirely on top of `Future` with the same performance,
effortlessly integrates into existing `Future` based libraries.

# Installation

Setup via `build.sbt`:

```sbt
libraryDependencies += "dev.ragz" %% "io" % "0.1.0"
```

# Getting Started
```scala
val successIO: IO[Nothing, Int] = IO.successful(a + 1)
val failedIO: IO[Throwable, Nothing] = IO.failed(new Exception("error"))
val typedFailureIO: IO[RunTimeException, Nothing] = IO.failed(new RuntimeException("error"))
val TaskIO: IO[Throwable, Int] = IO(1)
```
This is the basics of using `IO` in your code. The IO has the same API
as the `Future`, together with the type alias
`type Future[+A] = IO[Throwable, A]` makes the type compatible with the `Future`.
## Error handling

```scala 
val throwableIO: IO[Throwable, Nothing] = IO.failed(new Exception("error"))
val runTimeExceptinIO: IO[RunTimeException, Nothing] =
  throwableIO.mapError {
    case e: Throwable => new RunTimeException(e)
  }

```
```scala
Compile and or run test

```shell
sbt compile
```

```shell
sbt test
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
[info] FutureBenchmark.resultFlatMap   thrpt   10  22.395 ± 0.375  ops/s
[info] FutureBenchmark.resultMap       thrpt   10  27.328 ± 0.455  ops/s
[info] FutureBenchmark.resultMapError  thrpt   10  27.177 ± 0.041  ops/s
[info] FutureBenchmark.resultSequence  thrpt   10   1.817 ± 0.029  ops/s
[success] Total time: 1623 s (27:03), completed Feb 20, 2024, 7:02:20 PM
```
