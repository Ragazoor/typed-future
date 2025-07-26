package io.github.ragazoor.task

import io.github.ragazoor.task.implicits._
import munit.FunSuite

import scala.concurrent.{ Future => StdFuture }

class FutureToTaskSpec extends FunSuite {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def munitValueTransforms = super.munitValueTransforms ++ List(
    new ValueTransform(
      "Task",
      { case Task(future) =>
        future
      }
    )
  )
  test("Typed Future using flatmap") {
    val result = for {
      result <- StdFuture(1)
    } yield assert(result == 1)

    result.toTask
  }

}
