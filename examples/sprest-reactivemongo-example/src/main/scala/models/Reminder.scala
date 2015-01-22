package sprest.examples.reactivemongo.models

import spray.json._
import spray.json.DefaultJsonProtocol._
import sprest.models._
import org.joda.time.DateTime
import sprest.reactivemongo.typemappers._

case class Reminder(
  id: String,
  remindAt: DateTime,
  title: String,
  body: Option[String] = None) extends Model[String]

object Reminder {
  import sprest.Formats._
  implicit val reminderJsonFormat = jsonFormat4(Reminder.apply _)
}
