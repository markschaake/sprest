package sprest.reactivemongo

import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.core.commands.Command
import reactivemongo.core.commands.Count
import sprest.Implicits._
import sprest.Logging
import sprest.models._
import sprest.security.Session
import sprest.reactivemongo.typemappers._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import play.api.libs.iteratee.Iteratee
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.bson.{ BSONReader, BSONWriter }
import reactivemongo.bson.DefaultBSONHandlers._
import spray.json._

case class Sort(fieldName: String, direction: Sort.SortDirection)
object Sort {
  sealed abstract class SortDirection(val value: Int)
  case object Asc extends SortDirection(1)
  case object Desc extends SortDirection(-1)

  def apply(fieldAndSort: (String, SortDirection)): Sort = apply(fieldAndSort._1, fieldAndSort._2)

  implicit val bsonWriter = new BSONDocumentWriter[Sort] {
    override def write(sort: Sort): BSONDocument = {
      BSONDocument(elements = List(
        sort.fieldName -> BSONInteger(sort.direction.value)))
    }
  }
}

trait ReactiveMongoPersistence {

  abstract class CollectionDAO[M <: Model[ID], ID](protected val collection: BSONCollection)(implicit jsonFormat: RootJsonFormat[M], jsonMapper: SprayJsonTypeMapper, idMapper: BSONTypeMapper[ID])
    extends DAO[M, ID] with BsonProtocol with Logging {

    override val loggerName = "Sprest-ReactiveMongo"

    implicit val bsonFormat = generateBSONFormat[M]

    private def findByIdQuery(id: ID) = BSONDocument("_id" -> idMapper.toBSON(id))
    private val emptyQuery = BSONDocument()

    /* ===========> DAO interface <============ */
    override protected def allImpl(implicit ec: ExecutionContext) = collection.find(emptyQuery).cursor[M].collect[List]()

    override def findBySelector(selector: Selector)(implicit ec: ExecutionContext) =
      Logger.debugTimedAsync(s"Fetching by selector $selector", logResult = true) {
        collection.find(findByIdQuery(selector.id)).cursor[M].collect[List]().map(_.headOption)
      }

    def count()(implicit ec: ExecutionContext): Future[Int] =
      collection.db.command(Count(collection.name))

    def count[T](query: T)(implicit writer: BSONDocumentWriter[T], ec: ExecutionContext): Future[Int] =
      collection.db.command(Count(collection.name, Some(writer.write(query))))

    def queryBuilder[T](obj: T)(implicit writer: BSONDocumentWriter[T], ec: ExecutionContext) =
      collection.find(obj)

    /**
     * Returns the query result as a [[reactivemongo.api.Cursor]] object
     *
     * @param obj the query object that can be converted into a BSONDocument
     * @param writer implicit BSONDocumentWriter for T
     * @param ec implicit ExecutionContext
     */
    def findCursor[T](obj: T)(implicit writer: BSONDocumentWriter[T], ec: ExecutionContext): Cursor[M] = queryBuilder(obj).cursor[M]

    def find[T](obj: T)(implicit writer: BSONDocumentWriter[T], ec: ExecutionContext): Future[List[M]] = fetchMany {
      findCursor(obj).collect[Iterable]()
    }.map(_.toList)

    def find[T](obj: T, sort: Sort)(implicit writer: BSONDocumentWriter[T], ec: ExecutionContext): Future[List[M]] = fetchMany {
      // generate the sort BSONDocument
      val sortDoc = BSONDocument.empty.add(Sort.bsonWriter.write(sort))
      queryBuilder(obj).sort(sortDoc).cursor[M].collect[Iterable]()
    }.map(_.toList)

    def find[T](obj: T, sorts: List[Sort])(implicit writer: BSONDocumentWriter[T], ec: ExecutionContext): Future[List[M]] = fetchMany {
      // generate the sort BSONDocument
      val sortDoc = sorts.foldLeft(BSONDocument.empty) { (doc, sort) =>
        doc.add(Sort.bsonWriter.write(sort))
      }
      queryBuilder(obj).sort(sortDoc).cursor[M].collect[Iterable]()
    }.map(_.toList)

    def findOne[T](obj: T)(implicit writer: BSONDocumentWriter[T], ec: ExecutionContext): Future[Option[M]] = fetchOne {
      findCursor(obj).headOption
    }

    def findAsCursor[P](selector: JsObject, projection: JsObject)(implicit reads: RootJsonReader[P], ec: ExecutionContext) =
      collection.find(selector, projection).cursor[P]

    /**
     * Projects the query into an object of type P
     */
    def findAs[P](selector: JsObject, projection: JsObject)(implicit reads: RootJsonReader[P], ec: ExecutionContext) =
      findAsCursor[P](selector, projection).collect[List]()

    def findAs[P](selector: JsObject)(implicit projection: Projection[M, P], ec: ExecutionContext) =
      collection.find(selector, projection.projection).cursor[P](projection.reads, ec).collect[List]()

    def findOneAs[P](selector: JsObject)(implicit projection: Projection[M, P], ec: ExecutionContext) =
      findAs[P](selector) map { _.headOption }

    def findOneAs[P](selector: JsObject, projection: JsObject)(implicit reads: RootJsonReader[P], ec: ExecutionContext) =
      findAs[P](selector, projection) map { _.headOption }

    protected def uncheckedRemoveById(id: ID)(implicit ec: ExecutionContext) = collection.uncheckedRemove(findByIdQuery(id))

    protected def checkedRemoveById(id: ID)(implicit ec: ExecutionContext) = collection.remove(findByIdQuery(id))

    /** Inserts the model */
    protected def doAdd(m: M)(implicit ec: ExecutionContext): Future[M] = m.id match {
      case Some(id) =>
        collection.insert(m) flatMap { lastError =>
          if (lastError.ok)
            Future.successful(m)
          else
            Future.failed(lastError.getCause())
        }
      case None =>
        m.id = nextId
        collection.insert(m) flatMap { lastError =>
          if (lastError.ok)
            Future.successful(m)
          else
            Future.failed(lastError.getCause())
        }
    }

    /** Updates the model without checks */
    protected def doUpdate(m: M)(implicit ec: ExecutionContext): Future[M] = m.id match {
      case Some(id) =>
        collection.update(
          selector = findByIdQuery(id),
          update = m,
          upsert = true,
          multi = false) flatMap { lastError =>
            if (lastError.ok)
              Future.successful(m)
            else
              Future.failed(lastError.getCause())
          }
      case None => throw new Exception("id required for update")
    }
  }
}

