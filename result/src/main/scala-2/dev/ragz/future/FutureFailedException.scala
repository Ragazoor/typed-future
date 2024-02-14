package dev.ragz.future

object FutureFailedException {
  private[future] type FutureFailedException = NoSuchElementException with FatalError

}
