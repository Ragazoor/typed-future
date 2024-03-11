
import munit.FunSuite

class IONonFatalSpec extends FunSuite {
  case class MyError(e: Throwable) extends Throwable(e.getMessage, e)

  test("MyError is non fatal") {
    val e = new RuntimeException("test message")
    assert(NonFatal(e))
  }

  test("MyError is non fatal") {
    val e       = new RuntimeException("test message")
    val myError = MyError(e)
    assert(NonFatal(myError))
  }

  test("FatalError is fatal") {
    val e          = new RuntimeException("test message")
    val fatalError = FatalError(e)
    assert(!NonFatal(fatalError))
  }

  test("InterruptedException is fatal") {
    val fatalError = new InterruptedException("test message")
    assert(!NonFatal(fatalError))
  }

}
