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
import scala.concurrent.{Future => StdFuture}
import io.github.ragazoor.{ExecutionContext, Future}
import io.github.ragazoor.implicits._

case class User(name: String, age: Int)

trait UserRepository {
  def getUser(id: Int): StdFuture[User]
}

class UserService(userRepo: UserRepository)(implicit ec: ExecutionContext) {
  def getUser(id: Int): Future[User] = // Future[User] is an alias for IO[Throwable, User] 
    userRepo
      .getUser(id)
      .io // Converts to IO
}
```

This is the basics of how to enable the use of a typed future in
your future based code. The `IO` has the same API
as the `StdFuture`, and thanks to the type alias
`type Future[+A] = IO[Throwable, A]` we don't need to rename `Future`s
all over the code base.

### Error handling

Using the example above it is now trivial to map a failed `StdFuture`
to an `IO` with an error from our domain model.

```scala 
case class UserNotFound(message: String, cause: Throwable) extends Exception(message, cause)

object UserNotFound {
  def apply(id: Int)(cause: Throwable): UserNotFound = UserNotFound(s"User with id $id not found", cause)
}

class UserService(userRepo: UserRepository)(implicit ec: ExecutionContext) {
  def getUser(id: Int): IO[UserNotFound, User] = // Future[User] is an alias for IO[Throwable, User]
    userRepo
      .getUser(id)
      .io // Converts to IO
      .mapError(UserNotFound(id))
```

Similar to `ZIO` it is also possible to create IO's with errors that we cannot
recover from, except with a few methods like `catch` and `recover`. This is done by using `IO.fatal`:

```scala
case class UnrecoverableError(message: String, cause: Throwable) extends Exception(message, cause)

class UserService(userRepo: UserRepository)(implicit ec: ExecutionContext) {
  def getUser(id: Int): IO[UserNotFound, User] =
    if (id < 0) {
      IO.fatal(UnrecoverableError("Not best example but lets say this is a fatal error", new RuntimeException("Fatal error")))
    } else {
      userRepo
        .getUser(id)
        .io // Converts to IO
        .mapError(UserNotFound(id))
    }
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
