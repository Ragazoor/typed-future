package io.github

package object ragazoor {
  type Future[+A] = Attempt[Throwable, A]
}
