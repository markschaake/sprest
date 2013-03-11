package sprest.examples.reactivemongo

import spray.json._
import java.util.UUID
import sprest.models._
import scala.concurrent.ExecutionContext.Implicits.global
import sprest.reactivemongo.ReactiveMongoPersistence
import sprest.reactivemongo.typemappers._

object JsonAPI {
  case class ToDo(text: String, done: Boolean, var id: Option[String] = None) extends Model[String]

  object ToDos extends MutableListDAO[ToDo, String] {

    add(ToDo("first", false, None))
    add(ToDo("Second", true, None))
  }

  object Formats extends DefaultJsonProtocol {
    implicit val ToDoFormat = jsonFormat3(ToDo)
  }
}

object DB extends ReactiveMongoPersistence {

  import reactivemongo.api._
  import reactivemongo.bson._
  import reactivemongo.bson.handlers._
  import JsonAPI._

  lazy val connection = MongoConnection(List("localhost:27017"))
  lazy val db = connection("reactive-example")

  object ToDos extends CollectionDAO[ToDo, String] with UUIDStringId {
    override lazy val collection = db("todos")
    override implicit val idMapper = ValueTypeMapper.StringTypeMapper
    override val bsonWriter = new BSONWriter[ToDo] {
      def toBSON(td: ToDo) = {
        val id = td.id.getOrElse(java.util.UUID.randomUUID().toString)
        BSONDocument(
          "_id" -> BSONString(id),
          "text" -> BSONString(td.text),
          "done" -> BSONBoolean(td.done))
      }
    }

    override val bsonReader = new BSONReader[ToDo] {
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
  }

}

