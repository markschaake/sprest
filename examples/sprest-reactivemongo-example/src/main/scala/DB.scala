package sprest.examples.reactivemongo

import sprest.reactivemongo.ReactiveMongoPersistence
import sprest.reactivemongo.typemappers.ValueTypeMapper._

object DB extends ReactiveMongoPersistence {

  import reactivemongo.api._
  import reactivemongo.bson._
  import reactivemongo.bson.handlers._
  import scala.concurrent.ExecutionContext.Implicits.global
  import sprest.models._
  import models._

  lazy val connection = MongoConnection(List("localhost:27017"))
  lazy val db = connection("reactive-example")

  object ToDos extends CollectionDAO[ToDo, String](db("todos")) with UUIDStringId
  object Reminders extends CollectionDAO[Reminder, String](db("reminders")) with UUIDStringId

}
