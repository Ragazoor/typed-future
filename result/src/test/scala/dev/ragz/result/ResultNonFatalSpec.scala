package dev.ragz.result

import munit.FunSuite

class ResultNonFatalSpec extends FunSuite {
  case class MyError(e: Throwable) extends ResultErrors(e.getMessage, e)
  test("MyError is non fatal") {
    val e = new RuntimeException("test message")
    assert(ResultNonFatal(e))
  }

  test("MyError is non fatal") {
    val e = new RuntimeException("test message")
    val myError = MyError(e)
    assert(ResultNonFatal(myError))
  }

  test("FatalError is fatal") {
    val e = new RuntimeException("test message")
    val fatalError = FatalError(e)
    assert(!ResultNonFatal(fatalError))
  }

  test("InterruptedException is fatal") {
    val fatalError = new InterruptedException("test message")
    assert(!ResultNonFatal(fatalError))
  }

}
