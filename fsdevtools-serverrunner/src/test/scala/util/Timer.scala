package util

import scala.concurrent.duration._

/**simple start-stop timer*/
class Timer {
  def now       = System.currentTimeMillis().milliseconds
  val startTime = now
  def elapsed   = now - startTime

  override def toString: String = s"${elapsed.toSeconds}s"
}
object Timer {
  def apply(): Timer = new Timer()
}
