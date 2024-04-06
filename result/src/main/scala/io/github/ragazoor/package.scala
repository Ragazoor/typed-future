package io.github

package object ragazoor {
  type Future[+A] = IO[Throwable, A]
}
