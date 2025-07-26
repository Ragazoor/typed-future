package io.github.ragazoor

package object task {
  type Future[+A] = Task[Throwable, A]
}
