package sprest.examples.reactivemongo.models

import spray.json._
import sprest.models._

import sprest.util.enum._

/** Example usage of [[sprest.util.enum.Enum]] */
sealed abstract class Priority(val id: String, val label: String, val value: Int) extends Enum[Priority](id)

/** There must be a companion object for each defined [[sprest.util.enum.Enum]] */
object Priority extends EnumCompanion[Priority] {
  case object Urgent extends Priority("urgent", "Urgent", 0)
  case object Important extends Priority("important", "Important", 1)
  case object Normal extends Priority("normal", "Normal", 2)
  case object BackBurner extends Priority("back", "Back Burner", 3)

  // This is necessary for Priority.all to return all Enums due to
  // Scala objects being lazily class loaded
  register(
    Urgent,
    Important,
    Normal,
    BackBurner)
}

case class ToDo(text: String, done: Boolean, var id: Option[String] = None, priority: Priority = Priority.Normal) extends Model[String]

object ToDo extends ModelCompanion[ToDo, String] {

  implicit val toDoFormat = jsonFormat4(ToDo.apply _)

}

