package sprest.examples.reactivemongo.models

import spray.json._
import sprest.models._

case class ToDo(text: String, done: Boolean, var id: Option[String] = None) extends Model[String]

object ToDo extends ModelCompanion[ToDo, String] {

  implicit val toDoFormat = jsonFormat3(ToDo.apply _)

}

