package dev.ragz.io

import munit.FunSuite

import scala.concurrent.{ Future => StdFuture }
import dev.ragz.io.implicits._

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
    val result: Future[Unit] = for {
      1 <- StdFuture(1)
    } yield assert(1 == 1)

    result
  }

}
