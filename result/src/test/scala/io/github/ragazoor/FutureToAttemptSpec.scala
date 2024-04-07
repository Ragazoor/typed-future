package io.github.ragazoor

import io.github.ragazoor.implicits._
import munit.FunSuite

import scala.concurrent.{ Future => StdFuture }

class FutureToAttemptSpec extends FunSuite {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def munitValueTransforms = super.munitValueTransforms ++ List(
    new ValueTransform(
      "Attempt",
      { case Attempt(future, _) =>
        future
      }
    )
  )
  test("Typed Future using flatmap") {
    val result = for {
      result <- StdFuture(1)
    } yield assert(result == 1)

    result.attempt
  }

}