// Testing.sc
//
// A simple trait intended to be mixed into an `object Internal {...}` or similar


trait Testing {
  def mustBeEqual(x1: Any, x2: Any) = if (x1 != x2) { throw new IllegalStateException(s"$x1 != $x2") }
  
  def testing(what: String)(block: => Any) = {
    tests = tests ++ Vector { () =>
      startMsg(s" [$what]")
      block
      endMsg
    }
  }

  def testing(block: => Any) = {
    tests = tests ++ Vector { () =>
      startMsg("...")
      block
      endMsg
    }
  }

  private def startMsg(s: String) = print(s"Testing$s  ")
  private def endMsg = println("Success!")

  private type Test = () => Any
  private var tests = Vector[Test]()

  def runTests = tests.foreach(t => t())
}
