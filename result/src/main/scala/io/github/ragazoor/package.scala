package io.github

package object ragazoor {
  type Future[+A] = Task[Throwable, A]
}
