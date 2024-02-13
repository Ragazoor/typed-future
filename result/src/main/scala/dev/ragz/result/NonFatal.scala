package dev.ragz.result

import scala.util.control

object NonFatal {

  /**
   * Returns true if the provided `Throwable` is to be considered non-fatal, or false if it is to be considered fatal
   */
  def apply(t: Throwable): Boolean = t match {
    case _: FatalError            => false
    case e if control.NonFatal(e) => true
    case _                        => false
  }

  /**
   * Returns Some(t) if ResultNonFatal(t) == true, otherwise None
   */
  def unapply(t: Throwable): Option[Throwable] = Some(t).filter(apply)
}
