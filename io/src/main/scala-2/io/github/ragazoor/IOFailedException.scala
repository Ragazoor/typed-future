
object IOFailedException {
  private[io] type IOFailedException = NoSuchElementException with FatalError

}
