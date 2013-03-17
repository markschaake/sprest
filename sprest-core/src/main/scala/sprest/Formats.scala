package sprest

import spray.json._
import org.joda.time.DateTime

object Formats extends DefaultJsonProtocol {
  implicit object DateTimeFormat extends JsonFormat[DateTime] {
    override def read(json: JsValue) = json match {
      case JsNumber(value) if value.isValidLong => new DateTime(value.toLong)
      case JsString(dateString)                 => new DateTime(dateString)
      case _                                    => throw new Exception("Invalid JsValue type for DateTime conversion: must be JsNumber or JsString")
    }

    override def write(dt: DateTime) = JsNumber(dt.getMillis)
  }
}
