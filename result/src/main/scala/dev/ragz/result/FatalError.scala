package dev.ragz.result

private[result] final case class FatalError(e: Throwable) extends Throwable(e)
