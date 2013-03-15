package sprest.examples.reactivemongo.models

import spray.json._
import sprest.models._

case class ToDo(text: String, done: Boolean, var id: Option[String] = None) extends Model[String]

object ToDo extends DefaultJsonProtocol {
  import reactivemongo.api._
  import reactivemongo.bson._
  import reactivemongo.bson.handlers._

  implicit object ToDoBSONFormat extends BSONReader[ToDo] with BSONWriter[ToDo] {
    def toBSON(td: ToDo) = {
      val id = td.id.getOrElse(java.util.UUID.randomUUID().toString)
      BSONDocument(
        "_id" -> BSONString(id),
        "text" -> BSONString(td.text),
        "done" -> BSONBoolean(td.done))
    }

    def fromBSON(bson: BSONDocument) = {
      val mapped = bson.mapped
      ToDo(
        mapped("text").asInstanceOf[BSONString].value,
        mapped("done").asInstanceOf[BSONBoolean].value,
        mapped.get("_id") flatMap {
          case BSONString(id) => Some(id)
          case _              => None
        })
    }
  }

  implicit val ToDoFormat = jsonFormat3(ToDo.apply _)

}

