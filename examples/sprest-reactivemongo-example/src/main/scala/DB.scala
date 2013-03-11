package sprest.examples.reactivemongo

import sprest.reactivemongo.ReactiveMongoPersistence
import sprest.reactivemongo.typemappers._

object DB extends ReactiveMongoPersistence {

  import reactivemongo.api._
  import reactivemongo.bson._
  import reactivemongo.bson.handlers._
  import scala.concurrent.ExecutionContext.Implicits.global
  import JsonAPI._
  import sprest.models._

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
