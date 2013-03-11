package sprest.examples.reactivemongo

import spray.json._
import sprest.models._

object JsonAPI {
  case class ToDo(text: String, done: Boolean, var id: Option[String] = None) extends Model[String]

  object Formats extends DefaultJsonProtocol {
    implicit val ToDoFormat = jsonFormat3(ToDo)
  }
}

