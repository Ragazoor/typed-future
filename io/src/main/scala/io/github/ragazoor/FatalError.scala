package io.github.ragazoor

private[ragazoor] final case class FatalError(e: Throwable) extends Throwable(e)
