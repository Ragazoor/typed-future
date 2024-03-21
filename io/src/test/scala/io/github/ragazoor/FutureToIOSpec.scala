package io.github.ragazoor

import munit.FunSuite

import scala.concurrent.{ Future => StdFuture }
import implicits._

class FutureToIOSpec extends FunSuite {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def munitValueTransforms = super.munitValueTransforms ++ List(
    new ValueTransform(
      "IO",
      { case IO(future) =>
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
