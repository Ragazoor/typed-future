package dev.ragz

package object result {
  type Future[+A] = Result[Throwable, A]
}
