# An error typed Future

A Future based monad with typed errors.
Designed to be a replacement for the `scala.concurrent.Future`
(`StdFuture`) with minimal migration needed. Entirely built on top
of the `StdFuture` it has
the same performance and easily integrates into existing `StdFuture`
based libraries.
It also extends the api of the `StdFuture`, which is heavily
inspired by ZIO ([github link](https://github.com/zio/zio)).

If you are already used to working with typed errors I would highly
recommend checking out [ZIO](https://zio.dev/overview/getting-started)
or [Monix BIO](https://bio.monix.io/docs/introduction) instead.
However if you do not want to commit to another effect system and
still want complete control of your types this library is for you.

# Installation

> [!NOTE]
> Due to the new sonatype interace the library is not yet available
> in maven central.

Setup via `build.sbt`:

```sbt
libraryDependencies += "io.github.ragazoor" %% "future" % "0.1.0"
```

# Getting Started

## Examples

```scala

```

In `io.github.ragazoor.migration.implicits._` there are implicits that
are used to convert an `IO` to a `Future`. This is useful in a migration
phase when you have a third party library which depends on getting a
`Future`.

```scala

```

This is the basics for using the typed future in
your code. The `IO` has the same API
as the `Future`, and thanks to the type alias
`type Future[+A] = IO[Throwable, A]` we don't need to rename `Future`s
all over the code base.

### Error handling

Using the example above it is now trivial to map a failed `StdFuture`
to an `IO` with an error from our domain model.

```scala 

```

## Migration

The goal of the library is not to replace everything in `scala.concurrent.*`
since this would require a re-implementation of several key components. The
goal is rather to provide a typed alternative to the Future and
use the rest from the standard library.

The migration depends on how much of the `scala.concurrent` library you are
using. This example is for a migration where the project is only using
ExecutionContext and Future from `scala.concurrent`.

```text
replace: 
import scala.concurrent.*

with: 
import scala.concurrent.{ExecutionContext, Future => StdFuture}
import io.github.ragazoor.*
import io.github.ragazoor.implicits.*
import io.github.ragazoor.migration.implicits.*
```

There are a few occurrences where we need to manually fix the code:

- If we are using a third-party library returning a `scala.concurrent.Future`
  we need to convert it to `IO` using `.io` and the implicit
  `io.github.ragazoor.implicits.StdFutureToIO`.
- If there are async tests using `scala.concurrent.Future` but does not
  have `scala.concurrent` imported we need to add
  `import io.github.ragazoor.migration.implicits._`.
- If you are using implicit classes that extends `scala.concurrent.Future`
  the compiler will not be able to convert
  like one might think using the migration implicits. So we need to make
  it explicit:

```scala
object ImplicitClassExample {
  implicit class MyImplicitClassFunction(f: StdFuture[Int])(implicit ec: ExecutionContext) {
    def bar: StdFuture[Option[Int]] = f.map(Some(_))
  }

  import scala.concurrent.ExecutionContext.Implicits.global
  def foo: Attempt[Throwable, Int] = ???
  
  val a: Attempt[Throwable, Option[Int]] = foo.bar.attempt // does not compile
  val b: Attempt[Throwable, Option[Int]] = foo.toFuture.bar.attempt
}
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
