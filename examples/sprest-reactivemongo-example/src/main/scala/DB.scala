package sprest.examples.reactivemongo

import sprest.reactivemongo.{ ReactiveMongoPersistence, BsonProtocol }
import sprest.reactivemongo.typemappers._

object DB extends ReactiveMongoPersistence with BsonProtocol {

  import reactivemongo.api.MongoConnection
  import scala.concurrent.ExecutionContext.Implicits.global
  import sprest.models.UUIDStringId
  import models._

  lazy val connection = MongoConnection(List("localhost:27017"))
  lazy val db = connection("reactive-example")

  // Json mapping to / from BSON - in this case we want "_id" from BSON to be 
  // mapped to "id" in JSON in all cases
  implicit object JsonTypeMapper extends SprayJsonTypeMapper with NormalizedIdTransformer

  // MongoDB collections:
  object ToDos extends CollectionDAO[ToDo, String](db("todos")) with UUIDStringId
  object Reminders extends CollectionDAO[Reminder, String](db("reminders")) with UUIDStringId

}
