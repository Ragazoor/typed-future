package io.github.ragazoor

import io.github.ragazoor.implicits._
import munit.FunSuite

import scala.concurrent.{Future => StdFuture}

class FutureToIOSpec extends FunSuite {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def munitValueTransforms = super.munitValueTransforms ++ List(
    new ValueTransform(
      "IO",
      { case IO(future, _) =>
        future
      }
    )
  )
  test("Typed Future using flatmap") {
    val result = for {
      result <- StdFuture(1)
    } yield assert(result == 1)

    result.io
  }

}
