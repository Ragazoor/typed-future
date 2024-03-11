
import scala.concurrent.{ Future => StdFuture }

object implicits {
  implicit class StdFutureToIo[A](val future: StdFuture[A]) {
    def io: IO[Throwable, A] = IO.fromFuture(future)
  }

}
