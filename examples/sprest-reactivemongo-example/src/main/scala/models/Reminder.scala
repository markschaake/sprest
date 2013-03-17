package sprest.examples.reactivemongo.models

import spray.json._
import sprest.models._
import org.joda.time.DateTime
import sprest.reactivemongo.typemappers._

case class Reminder(
  remindAt: DateTime,
  title: String,
  body: Option[String] = None,
  var id: Option[String] = None) extends Model[String]

object Reminder extends ModelCompanion[Reminder, String] {

  implicit object DateTimeFormat extends JsonFormat[DateTime] {
    def read(json: JsValue) = json match {
      case JsNumber(value) if value.isValidLong => new DateTime(value.toLong)
      case JsString(strDate)                    => new DateTime(strDate)
      case _                                    => throw new Exception("invalid format")
    }
    def write(date: DateTime) = JsNumber(date.getMillis)
  }

  implicit val reminderJsonFormat = jsonFormat4(Reminder.apply _)

}

