// Tasks.sc
//
// See tests for usage

import $file.Testing
import Testing._
import $file.Files, Files._, java.io._

trait AbstractTask[State] extends Function1[State => State, State => State] {
  def transform(s: State): State = s
  def withResults(s: State): Unit = ()

  // Implementation detail
  def apply(nextTask: State => State) = {
    (s: State) => {
      val sPrime = transform(s)
      val result = nextTask(sPrime)
      withResults(result)
      result
    }
  }
}

def comp[T](fs: (T => T)*) = fs.foldLeft(identity[T](_))( _ compose _ )
def job[T](tasks: (T => T) => (T => T)): T => T = tasks(identity)


object Internal extends Testing {
  testing {
    trait Task extends AbstractTask[String]

    case class Msg(m: String) extends Task {
      override def transform(s: String) = s"$s:$m"
    }
  
    val msgs = job {
      Msg("One") compose
      Msg("Two") compose
      Msg("Three")
    }

    mustBeEqual(msgs("Start"), "Start:One:Two:Three")

    val msgs2 = job(comp(Msg("One"), Msg("Two"), Msg("Three")))

    mustBeEqual(msgs("End"), "End:One:Two:Three")
  }
}

Internal.runTests

