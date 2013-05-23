package sprest.reactivemongo

import reactivemongo.api.collections.default.BSONCollection
import sprest.models._
import sprest.security.Session
import sprest.reactivemongo.typemappers._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.iteratee.Iteratee

trait ReactiveMongoPersistence {
  import reactivemongo.api._
  import reactivemongo.bson._
  import reactivemongo.bson.{ BSONReader, BSONWriter }
  import reactivemongo.bson.DefaultBSONHandlers._
  import spray.json._

  abstract class CollectionDAO[M <: Model[ID], ID, SessionImpl <: Session](collection: BSONCollection)(implicit jsonFormat: RootJsonFormat[M], jsonMapper: SprayJsonTypeMapper, idMapper: BSONTypeMapper[ID])
    extends DAO[M, ID, SessionImpl] with BsonProtocol {

    implicit val bsonFormat = generateBSONFormat[M]

    private def findByIdQuery(id: ID) = BSONDocument("_id" -> idMapper.toBSON(id))
    private val emptyQuery = BSONDocument()

    /* ===========> DAO interface <============ */
    override def all(implicit maybeSession: Option[SessionImpl]) = collection.find(emptyQuery).cursor[M].toList

    override def findBySelector(selector: Selector) = collection.find(findByIdQuery(selector.id)).cursor[M].toList.map(_.headOption)

    def find(bsonDoc: BSONDocument) = collection.find(bsonDoc).cursor[M].toList

    protected def uncheckedRemoveById(id: ID) = collection.uncheckedRemove(findByIdQuery(id))

    protected def checkedRemoveById(id: ID) = collection.remove(findByIdQuery(id))

    /** Inserts the model */
    protected def doAdd(m: M): Future[M] = m.id match {
      case Some(id) =>
        collection.insert(m)
        Future.successful(m)
      case None =>
        m.id = nextId
        collection.uncheckedInsert(m)
        Future.successful(m)
    }

    /** Updates the model without checks */
    protected def doUpdate(m: M): Future[M] = m.id match {
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

