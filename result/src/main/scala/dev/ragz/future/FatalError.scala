package dev.ragz.future

private[future] final case class FatalError(e: Throwable) extends Throwable(e)
