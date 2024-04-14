package common

final case class UserNotFound(msg: String, cause: Throwable) extends Exception(msg, cause)
