package io.github

package object ragazoor {
  type Future[+A]       = IO[Throwable, A]
  type ExecutionContext = scala.concurrent.ExecutionContext

  /**
   * Type alias for scala.concurrent.ExecutionContextExecutor.
   */
  type ExecutionContextExecutor = scala.concurrent.ExecutionContextExecutor

  /**
   * Type alias for scala.concurrent.ExecutionContextExecutorService.
   */
  type ExecutionContextExecutorService = scala.concurrent.ExecutionContextExecutorService

}
