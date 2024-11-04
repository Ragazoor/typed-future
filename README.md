# An error typed Future[+E, +A]
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org) ![CI Badge](https://github.com/ragazoor/typed-future/workflows/CI/badge.svg) ![Maven Central Version](https://img.shields.io/maven-central/v/io.github.ragazoor/task_2.13)


A thin wrapper on the Future monad for the purpose of giving it a type parameter for the error channel, giving you
the power to see _how_ a Future can fail just as clearly as how it can succeed.
Designed to be an alternative for the `scala.concurrent.Future` with minimal migration needed, entirely built on top
of the Future, it has
the same performance and easily integrates into existing Future
based libraries.
It also extends the api of the Future in a way which is heavily
inspired by ZIO ([github link](https://github.com/zio/zio)).

If you are already used to working with typed errors I would highly
recommend checking out [ZIO](https://zio.dev/overview/getting-started)
or [Monix BIO](https://bio.monix.io/docs/introduction) instead.
However if you do not want to commit to another effect system and
still want typed errors feel free to use this library, or copy the code to your project.

# Installation

Setup via `build.sbt`:

```sbt
libraryDependencies += "io.github.ragazoor" %% "task" % "0.1.16"
```

# Getting Started

In this library the monad is called a `Task`, which has the type signature `Task[+E, +A]`.
This `Task` is just a thin wrapper on top of the Future we know from Scala, which we have defined here as the
type alias `type Future[+A] = Task[Throwable, A]`. This is to keep backward compatability if you were to adopt this library.

## Examples

In `io.github.ragazoor.implicits._` there is an implicit class that
allows you to convert from an `scala.concurrent.Future` to a `Task` using `.toTask`.
```scala
import common.User
import io.github.ragazoor.Future
import io.github.ragazoor.implicits.StdFutureToTask

import scala.concurrent.Future as StdFuture

trait UserRepository {
  def getUser(id: Int): StdFuture[User]

class UserExample(userRepo: UserRepository) {
  def getUser(id: Int): Future[User] = // Future[User] is an alias for Task[Throwable, User]
    userRepo
      .getUser(id)  // This returns a scala.concurrent.Future
      .toTask // Converts to Task[Throwable, User]
}
```

In `io.github.ragazoor.migration.implicits._` there are implicits that
are used to convert an `Task` to a `scala.concurrent.Future`. This is useful in a migration
phase when you have a third party library which depends on Futures.
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
  def process(id: StdFuture[User]): StdFuture[User]  // Works with scala.concurrent.Future
}

class UserServiceFutureExample(userProcess: UserProcess)(implicit ec: ExecutionContext) {

  def processUser(userTask: Task[User]): Task[Throwable, User] =
    userProcess.process(userTask)    // Using scala.concurrent.Future as input and output, implicit conversion
      .toTask

  // Does the same thing without implicits, but more migration needed
  def processUser2(userTask: Task[User]): Task[Throwable, User] =
    userProcess.process(userTask.toFuture)  // Using scala.concurrent.Future as input and output, explicit conversion
      .toTask
}

```

This is the basics for using the `Task` type in
your code. The Task has the same API as the Future, and thanks to the type alias
`type Future[+A] = Task[Throwable, A]` we don't need to rename a lot of unnecessary renaming.

### Error handling

Using the example above it is now trivial to map a failed `scala.concurrent.Future`
to a `Task` with an error from our domain model.

```scala 
import common.{User, UserNotFound, UserRepository}
import io.github.ragazoor.Task
import io.github.ragazoor.implicits.StdFutureToTask

import scala.concurrent.ExecutionContext


class UserServiceTaskExample(userRepo: UserRepository)(implicit ec: ExecutionContext) {
  def getUser(id: Int): Task[UserNotFound, User] =
    userRepo
      .getUser(id)  // Returns a scala.concurrent.Future
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
  implicit class MyImplicitClassFunction[A](f: StdFuture[A])(implicit ec: ExecutionContext) {
    def bar: StdFuture[Option[A]] = f.map(Some(_))
  }
  def foo: Task[Throwable, Int] = ???
  /* does not compile */
  val a: Task[Throwable, Option[Int]] = foo.bar.toTask

  import scala.concurrent.ExecutionContext.Implicits.global
  val b: Task[Throwable, Option[Int]] = foo.toFuture.bar.toTask
}
```

## Benchmarks

Any contribution to more or improved benchmarks are most welcome!

Run benchmarks

```shell
sbt "benchmark/jmh:run -i 10 -wi 10 -f 1 -t 1 io.github.ragazoor.TaskBenchmark"
```

Example benchmark

```text
[info] Benchmark                      Mode  Cnt   Score   Error  Units
[info] TaskBenchmark.futureFlatMap   thrpt   10  34.419 ± 1.406  ops/s
[info] TaskBenchmark.futureMap       thrpt   10  34.556 ± 0.850  ops/s
[info] TaskBenchmark.futureRecover   thrpt   10  33.102 ± 0.802  ops/s
[info] TaskBenchmark.futureSequence  thrpt   10   1.858 ± 0.019  ops/s
[info] TaskBenchmark.taskFlatMap     thrpt   10  34.451 ± 0.961  ops/s
[info] TaskBenchmark.taskMap         thrpt   10  36.490 ± 1.042  ops/s
[info] TaskBenchmark.taskMapError    thrpt   10  35.284 ± 1.302  ops/s
[info] TaskBenchmark.taskSequence    thrpt   10   1.558 ± 0.047  ops/s
```
