package dev.ragz.io

object IOFailedException {
  private[io] type IOFailedException = NoSuchElementException | FatalError

}
