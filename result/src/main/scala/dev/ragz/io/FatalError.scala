package dev.ragz.io

private[io] final case class FatalError(e: Throwable) extends Throwable(e)
