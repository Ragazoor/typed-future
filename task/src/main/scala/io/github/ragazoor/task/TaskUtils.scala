package io.github.ragazoor.task

import scala.util.Failure
import scala.util.control.NoStackTrace

object TaskUtils {
  private[task] final val failedFailure                      =
    Failure[Nothing](
      new NoSuchElementException("Future.failed not completed with error E.") with NoStackTrace
    )
  private final val _zipWithTuple2: (Any, Any) => (Any, Any) = Tuple2.apply

  private[task] final def zipWithTuple2Fun[T, U]: (T, U) => (T, U) =
    _zipWithTuple2.asInstanceOf[(T, U) => (T, U)]
}
