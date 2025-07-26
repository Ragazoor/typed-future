package io.github.ragazoor.task.examples.utils.exceptions

abstract class DomainException(msg: String, cause: Throwable) extends Exception(msg, cause)

final case class UserNotFoundException(msg: String, cause: Throwable) extends DomainException(msg, cause)

final case class NotAllowedException(msg: String) extends DomainException(msg, new Throwable(msg))
