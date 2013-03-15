package sprest.examples.reactivemongo.models

import spray.json._
import sprest.models._
import sprest.reactivemongo.ModelCompanion

case class ToDo(text: String, done: Boolean, var id: Option[String] = None) extends Model[String]

object ToDo extends ModelCompanion[ToDo, String] {

  implicit val ToDoFormat = jsonFormat3(ToDo.apply _)
  implicit val ToDoBsonFormat = generateBSONFormat(ToDoFormat)

}

