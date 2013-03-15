package sprest

trait Logging {
  // override to change
  def loggerName = "app"

  import org.slf4j.LoggerFactory

  lazy val Logger = LoggerFactory.getLogger(loggerName)

}

