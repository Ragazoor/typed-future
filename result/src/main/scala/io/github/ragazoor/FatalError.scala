package io.github.ragazoor

private[io] final case class FatalError(e: Throwable) extends Throwable(e)
