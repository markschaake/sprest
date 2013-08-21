package sprest.reactivemongo

import reactivemongo.api.collections.default.BSONCollection
import sprest.Implicits._
import sprest.Logging
import sprest.models._
import sprest.security.Session
import sprest.reactivemongo.typemappers._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import play.api.libs.iteratee.Iteratee

trait ReactiveMongoPersistence {
  import reactivemongo.api._
  import reactivemongo.bson._
  import reactivemongo.bson.{ BSONReader, BSONWriter }
  import reactivemongo.bson.DefaultBSONHandlers._
  import spray.json._

  abstract class CollectionDAO[M <: Model[ID], ID](collection: BSONCollection)(implicit jsonFormat: RootJsonFormat[M], jsonMapper: SprayJsonTypeMapper, idMapper: BSONTypeMapper[ID])
    extends DAO[M, ID] with BsonProtocol with Logging {

    override val loggerName = "Sprest-ReactiveMongo"

    implicit val bsonFormat = generateBSONFormat[M]

    private def findByIdQuery(id: ID) = BSONDocument("_id" -> idMapper.toBSON(id))
    private val emptyQuery = BSONDocument()

    /* ===========> DAO interface <============ */
    override protected def allImpl(implicit ec: ExecutionContext) = collection.find(emptyQuery).cursor[M].toList

    override def findBySelector(selector: Selector)(implicit ec: ExecutionContext) =
      Logger.debugTimedAsync(s"Fetching by selector $selector", logResult = true) {
        collection.find(findByIdQuery(selector.id)).cursor[M].toList.map(_.headOption)
      }

    def find[T](obj: T)(implicit writer: BSONDocumentWriter[T], ec: ExecutionContext) = collection.find(obj).cursor[M].toList

    /**
     * Projects the query into an object of type P
     */
    def findAs[P](selector: JsObject, projection: JsObject)(implicit reads: RootJsonReader[P], ec: ExecutionContext) =
      collection.find(selector, projection).cursor[P].toList

    def findAs[P](selector: JsObject)(implicit projection: Projection[M, P], ec: ExecutionContext) =
      collection.find(selector, projection.projection).cursor[P](projection.reads, ec).toList

    def findOneAs[P](selector: JsObject)(implicit projection: Projection[M, P], ec: ExecutionContext) =
      findAs[P](selector) map { _.headOption }

    def findOneAs[P](selector: JsObject, projection: JsObject)(implicit reads: RootJsonReader[P], ec: ExecutionContext) =
      findAs[P](selector, projection) map { _.headOption }

    protected def uncheckedRemoveById(id: ID)(implicit ec: ExecutionContext) = collection.uncheckedRemove(findByIdQuery(id))

    protected def checkedRemoveById(id: ID)(implicit ec: ExecutionContext) = collection.remove(findByIdQuery(id))

    /** Inserts the model */
    protected def doAdd(m: M)(implicit ec: ExecutionContext): Future[M] = m.id match {
      case Some(id) =>
        collection.insert(m)
        Future.successful(m)
      case None =>
        m.id = nextId
        collection.uncheckedInsert(m)
        Future.successful(m)
    }

    /** Updates the model without checks */
    protected def doUpdate(m: M)(implicit ec: ExecutionContext): Future[M] = m.id match {
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

