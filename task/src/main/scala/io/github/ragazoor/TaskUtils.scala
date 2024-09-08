package io.github.ragazoor

import scala.util.Failure
import scala.util.control.NoStackTrace

object TaskUtils {
  private[ragazoor] final val failedFailure                  =
    Failure[Nothing](
      new NoSuchElementException("Future.failed not completed with error E.") with NoStackTrace
    )
  private final val _zipWithTuple2: (Any, Any) => (Any, Any) = Tuple2.apply

  private[ragazoor] final def zipWithTuple2Fun[T, U]: (T, U) => (T, U) =
    _zipWithTuple2.asInstanceOf[(T, U) => (T, U)]
}
