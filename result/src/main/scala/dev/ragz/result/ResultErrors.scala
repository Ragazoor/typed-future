package dev.ragz.result

abstract class ResultErrors(msg: String, cause: Throwable) extends Exception(msg, cause)

trait FatalErrorT
case class FatalError(msg: String, e: Throwable) extends ResultErrors(msg, e) with FatalErrorT

object FatalError {
  def apply(e: Throwable): FatalError = FatalError(e.getMessage, e)
}
