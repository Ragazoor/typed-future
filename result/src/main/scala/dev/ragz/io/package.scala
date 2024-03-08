package dev.ragz

package object io {
  type Future[+A] = IO[Throwable, A]
  type ExecutionContext = scala.concurrent.ExecutionContext

  /**
   * Type alias for [[scala.concurrent.ExecutionContextExecutor ExecutionContextExecutor]].
   */
  type ExecutionContextExecutor = scala.concurrent.ExecutionContextExecutor

  /**
   * Type alias for [[scala.concurrent.ExecutionContextExecutorService ExecutionContextExecutorService]].
   */
  type ExecutionContextExecutorService = scala.concurrent.ExecutionContextExecutorService

}
