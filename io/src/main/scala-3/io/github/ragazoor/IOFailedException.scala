package io.github.ragazoor

object IOFailedException {
  private[ragazoor] type IOFailedException = NoSuchElementException | FatalError

}
