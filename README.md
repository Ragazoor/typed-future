# An error typed Future

A Future based monad with typed errors.
Designed to be a replacement for the `scala.concurrent.Future`
(I'll call it StdFuture here) with minimal migration needed. Entirely built on top
of the StdFuture, it has
the same performance and easily integrates into existing StdFuture
based libraries.
It also extends the api of the StdFuture, which is heavily
inspired by ZIO ([github link](https://github.com/zio/zio)).

If you are already used to working with typed errors I would highly
recommend checking out [ZIO](https://zio.dev/overview/getting-started)
or [Monix BIO](https://bio.monix.io/docs/introduction) instead.
However if you do not want to commit to another effect system and
still want typed errors feel free to use this library.

# Installation

> [!NOTE]
> Due to the new sonatype interface the library is not yet available
> in maven central.

Setup via `build.sbt`:

```sbt
libraryDependencies += "io.github.ragazoor" %% "future" % "0.1.0"
```

# Getting Started

## Examples

```scala
import common.{User, UserRepository}
import io.github.ragazoor.Future
import io.github.ragazoor.implicits.StdFutureToTask

class UserServiceExample(userRepo: UserRepository) {
  def getUser(id: Int): Future[User] = // Future[User] is an alias for Task[Throwable, User]
    userRepo
      .getUser(id)
      .toTask // Converts to Task
}
```

In `io.github.ragazoor.migration.implicits._` there are implicits that
are used to convert an `Task` to a `StdFuture`. This is useful in a migration
phase when you have a third party library which depends on getting a
`StdFuture`.

```scala
import common.User
import io.github.ragazoor.Task
import io.github.ragazoor.Future
import io.github.ragazoor.migration.implicits._
import io.github.ragazoor.implicits.StdFutureToTask

import scala.concurrent.{ExecutionContext, Future => StdFuture}

/*
 * Imagine this is in a third party library
 */
trait UserProcess {
  def process(id: StdFuture[User]): StdFuture[User]
}

class UserServiceFutureExample(userProcess: UserProcess)(implicit ec: ExecutionContext) {

  /* implicit conversion in io.github.ragazoor.migration.implicits._ converts
   * the Task to a Future
   */
  def getUser(id: Int): Future[User] =
    userProcess.process {
      Task.successful(User("Test name", 44))
    }.toTask

  // Does the same thing without implicits, but more migration needed
  def getUserExplicit(id: Int): Future[User] =
    userProcess.process {
      Task.successful(User("Test name", 44)).toFuture // Here the conversion to future is explicit
    }.toTask
}

```

This is the basics for using the `Task` type in
your code. The Task has the same API
as the StdFuture, and thanks to the type alias
`type Future[+A] = Task[Throwable, A]` we don't need to rename StdFutures
all over the code base.

### Error handling

Using the example above it is now trivial to map a failed `StdFuture`
to an `Task` with an error from our domain model.

```scala 
import common.{User, UserNotFound, UserRepository}
import io.github.ragazoor.Task
import io.github.ragazoor.implicits.StdFutureToTask

import scala.concurrent.ExecutionContext


class UserServiceTaskExample(userRepo: UserRepository)(implicit ec: ExecutionContext) {
  def getUser(id: Int): Task[UserNotFound, User] =
    userRepo
            .getUser(id)
            .toTask // Converts to Task
            .mapError(e => UserNotFound(s"common.User with id $id not found", e)) // Converts Error from Throwable -> UserNotFound
}
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
  we need to convert it to `Task` using `.toTask` and the implicit
  `io.github.ragazoor.implicits.StdFutureToTask`.
- If there are async tests using `StdFuture` but does not
  have `scala.concurrent` imported we need to add
  `import io.github.ragazoor.migration.implicits._`.
- If you have interfaces in your code like `A => StdFuture[B]` there are
  implicits in `import io.github.ragazoor.migration.implicits._` which 
  help with this.
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
[info] FutureBenchmark.taskFlatMap     thrpt   10  22.395 ± 0.375  ops/s
[info] FutureBenchmark.taskMap         thrpt   10  27.328 ± 0.455  ops/s
[info] FutureBenchmark.taskMapError    thrpt   10  27.177 ± 0.041  ops/s
[info] FutureBenchmark.taskSequence    thrpt   10   1.817 ± 0.029  ops/s
[success] Total time: 1623 s (27:03), completed Feb 20, 2024, 7:02:20 PM
```
