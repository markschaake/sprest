package sprest.models

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import spray.json._
import org.joda.time.DateTime

trait Model[ID] {
  var id: Id[ID]
}

/**
 * Provides conveniences typically needed for Model companion objects, such as Json format helpers
 */
trait ModelCompanion[M <: Model[ID], ID] extends DefaultJsonProtocol

trait DAO[M <: Model[ID], ID] {

  final def add(m: M): Future[M] = addImpl(prePersist(m)) map { postPersist }
  final def update(m: M): Future[M] = updateImpl(prePersist(m)) map { postPersist }
  def remove(id: ID)
  def all: Future[Iterable[M]]
  def findById(id: ID): Future[Option[M]]

  protected def nextId: Option[ID] = None

  protected def addImpl(m: M): Future[M]
  protected def updateImpl(m: M): Future[M]

  protected def prePersist(m: M): M = m
  protected def postPersist(m: M): M = m
}

trait IntId { this: DAO[_, Int] =>
  protected var lastId = 0
  override protected def nextId: Option[Int] = {
    lastId += 1
    Some(lastId)
  }
}

trait LongId { this: DAO[_, Long] =>
  protected var lastId = 0
  override protected def nextId: Option[Long] = {
    lastId += 1
    Some(lastId)
  }
}

trait UUIDStringId { this: DAO[_, String] =>
  override protected def nextId: Option[String] = Some(UUID.randomUUID.toString)
}

trait UUIDId { this: DAO[_, UUID] =>
  override protected def nextId: Option[UUID] = Some(UUID.randomUUID)
}

trait MutableListDAO[M <: Model[ID], ID] extends DAO[M, ID] {

  private val _all = scala.collection.mutable.ListBuffer[M]()

  def all = Future.successful { _all }

  override protected def addImpl(m: M) = Future.successful {
    m.id = nextId
    _all += m
    m
  }

  def removeAll() = _all.clear()

  def remove(id: ID) {
    findById(id).onSuccess {
      case Some(found) => _all -= found
      case None        => // do nothing
    }
  }

  override protected def updateImpl(m: M) = Future.successful {
    m.id match {
      case Some(id) =>
        remove(id)
        _all += m
        m
      case None => throw new Exception("ID required for update")
    }
  }

  def findById(id: ID) = Future.successful(_all.find(_.id.get == id))
}
