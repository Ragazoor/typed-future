package dev.ragz.result

object FutureFailedException {
  private[result] type FutureFailedException = NoSuchElementException | FatalError

}