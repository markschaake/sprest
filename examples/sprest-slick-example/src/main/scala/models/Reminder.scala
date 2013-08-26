package sprest.examples.slick.models

import spray.json._
import sprest.models._
import org.joda.time.DateTime

case class Reminder(
  remindAt: DateTime,
  title: String,
  body: Option[String] = None,
  var id: Option[Int] = None) extends Model[Int]

object Reminder extends ModelCompanion[Reminder, Int] {
  import sprest.Formats._
  implicit val reminderJsonFormat = jsonFormat4(Reminder.apply _)
}

