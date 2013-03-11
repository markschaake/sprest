package sprest.reactivemongo

import sprest.models._
import sprest.reactivemongo.typemappers.ValueTypeMapper
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.iteratee.Iteratee

trait ReactiveMongoPersistence {
  import reactivemongo.api._
  import reactivemongo.bson._
  import reactivemongo.bson.handlers.{ BSONReader, BSONWriter }
  import reactivemongo.bson.handlers.DefaultBSONHandlers._

  trait CollectionDAO[M <: Model[ID], ID] extends DAO[M, ID] {

    // provided
    def collection: Collection
    implicit def bsonReader: BSONReader[M]
    implicit def bsonWriter: BSONWriter[M]
    implicit def idMapper: ValueTypeMapper[ID]

    private def findByIdQuery(id: ID) = BSONDocument("_id" -> idMapper.writeBSONValue(id))
    private val emptyQuery = BSONDocument()

    /* ===========> DAO interface <============ */
    def all = collection.find[BSONDocument, M](emptyQuery).toList
    def findById(id: ID) = collection.find[BSONDocument, M](findByIdQuery(id)).toList.map(_.headOption)
    def remove(id: ID) {
      // TODO need to be able to handle errors here?
      collection.uncheckedRemove(findByIdQuery(id))
    }

    def add(m: M): Future[M] = m.id match {
      case Some(id) =>
        collection.uncheckedInsert(m)
        Future.successful(m)
      case None =>
        m.id = nextId
        collection.uncheckedInsert(m)
        Future.successful(m)
    }

    def update(m: M): Future[M] = m.id match {
      case Some(id) =>
        collection.uncheckedUpdate(
          selector = findByIdQuery(id),
          update = m,
          upsert = true,
          multi = false)
        // TODO how to handle errors here?
        // Maybe we update the sprest-core DAO to return a Future[Try[M]]?
        Future.successful(m)
      case None => throw new Exception("id required for update")
    }
  }
}

