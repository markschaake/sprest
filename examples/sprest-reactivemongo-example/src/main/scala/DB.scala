package sprest.examples.reactivemongo

import sprest.reactivemongo.ReactiveMongoPersistence

object DB extends ReactiveMongoPersistence {

  import reactivemongo.api.MongoConnection
  import scala.concurrent.ExecutionContext.Implicits.global
  import sprest.models.UUIDStringId
  import models._

  lazy val connection = MongoConnection(List("localhost:27017"))
  lazy val db = connection("reactive-example")

  // MongoDB collections:
  object ToDos extends CollectionDAO[ToDo, String](db("todos")) with UUIDStringId
  object Reminders extends CollectionDAO[Reminder, String](db("reminders")) with UUIDStringId

}
