package dev.ragz

package object io {
  type Future[+A] = IO[Throwable, A]
}
