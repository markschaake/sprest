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
  import sprest.Formats._
  implicit val reminderJsonFormat = jsonFormat4(Reminder.apply _)
}

