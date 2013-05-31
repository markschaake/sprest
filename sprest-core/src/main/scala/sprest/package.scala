package object sprest {

  import org.joda.time.Duration
  import org.joda.time.format.PeriodFormatterBuilder
  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext
  import scala.util.Failure
  import scala.util.Success

  private val periodFormatter =
    new PeriodFormatterBuilder()
      .appendMinutes()
      .appendSuffix(" minute", " minutes")
      .appendSeconds()
      .appendSuffix(" second", " seconds")
      .appendMillis()
      .appendSuffix(" millis", " millis")
      .toFormatter()

  object Implicits {

    implicit class RichLogger(val l: org.slf4j.Logger) extends AnyVal {
      def doDebug(f: => Any) = if (l.isDebugEnabled) f
      def doTrace(f: => Any) = if (l.isTraceEnabled) f
      def debugChecked(msg: String) = doDebug {
        l.debug(msg)
      }

      private def timed[A](msg: => String, logFunc: (String) => Unit, body: => A, logResult: Boolean = false): A = {
        val start = System.currentTimeMillis
        logFunc(s"[start] $msg")
        try {
          val result = body
          val end = System.currentTimeMillis
          val dur = new Duration(start, end)
          logFunc(s"[end] ${msg}. Duration: " + periodFormatter.print(dur.toPeriod))
          if (logResult) logFunc(result.toString)
          result
        } catch {
          case t: Throwable =>
            val end = System.currentTimeMillis
            val dur = new Duration(start, end)
            l.error(s"[end] ${msg}. Duration: " + periodFormatter.print(dur.toPeriod), t)
            throw t
        }
      }

      private def timedAsync[A](msg: => String, logFunc: (String) => Unit, body: => Future[A], logResult: Boolean = false)(implicit ec: ExecutionContext): Future[A] = {
        val start = System.currentTimeMillis
        logFunc(s"[start] $msg")
        try {
          val result = body
          result.onComplete {
            case Success(value) =>
              val end = System.currentTimeMillis
              val dur = new Duration(start, end)
              logFunc(s"[end] ${msg}. Duration: " + periodFormatter.print(dur.toPeriod))
              if (logResult) logFunc(value.toString)
            case Failure(reason) =>
              val end = System.currentTimeMillis
              val dur = new Duration(start, end)
              l.error(s"[end] ${msg}. Duration: " + periodFormatter.print(dur.toPeriod), reason)
          }
          result
        } catch {
          case t: Throwable =>
            val end = System.currentTimeMillis
            val dur = new Duration(start, end)
            l.error(s"[end] ${msg}. Duration: " + periodFormatter.print(dur.toPeriod), t)
            throw t
        }
      }

      def infoTimed[A](msg: => String, logResult: Boolean = false)(body: => A): A = timed(msg, msg => l.info(msg), body)

      def infoTimedAsync[A](msg: => String, logResult: Boolean = false)(body: => Future[A])(implicit ec: ExecutionContext): Future[A] = timedAsync(msg, msg => l.info(msg), body, logResult)

      def debugTimed[A](msg: => String, logResult: Boolean = false)(body: => A): A = timed(msg, msg => l.debug(msg), body)
      def debugTimedAsync[A](msg: => String, logResult: Boolean = false)(body: => Future[A])(implicit ec: ExecutionContext): Future[A] = timedAsync(msg, msg => l.debug(msg), body, logResult)

    }
  }
}
