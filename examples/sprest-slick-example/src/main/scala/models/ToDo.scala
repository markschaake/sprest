package sprest.examples.slick.models

import spray.json._
import sprest.models._

case class ToDo(text: String, done: Boolean, var id: Option[Int] = None) extends Model[Int]

object ToDo extends ModelCompanion[ToDo, Int] {

  implicit val toDoFormat = jsonFormat3(ToDo.apply _)

}

