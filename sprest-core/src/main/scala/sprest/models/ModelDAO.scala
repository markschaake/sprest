package sprest.models

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import sprest.security.Session

trait UniqueSelector[M <: Model[ID], ID] {
  def id: ID
  def session: Session
}

trait DAO[M <: Model[ID], ID] {

  /** An object used for selecting a unique record */
  type Selector <: UniqueSelector[M, ID]

  implicit def generateSelector(session: Session, id: ID): Selector

  final def add(m: M)(implicit session: Session): Future[M] = addImpl(prePersist(m)) map { postPersist }
  final def update(m: M)(implicit session: Session): Future[M] = updateImpl(prePersist(m)) map { postPersist }

  def remove(selector: Selector)
  def removeById(id: ID)(implicit session: Session) = remove(generateSelector(session, id))

  def all(implicit session: Session): Future[Iterable[M]]

  def findBySelector(selector: Selector): Future[Option[M]]

  def findById(id: ID)(implicit session: Session) = findBySelector(generateSelector(session, id))

  protected def nextId: Option[ID] = None

  protected def addImpl(m: M)(implicit session: Session): Future[M]
  protected def updateImpl(m: M)(implicit session: Session): Future[M]

  protected def prePersist(m: M)(implicit session: Session): M = m
  protected def postPersist(m: M)(implicit session: Session): M = m
}

trait MutableListDAO[M <: Model[ID], ID] extends DAO[M, ID] {

  case class MSelector(session: Session, id: ID) extends UniqueSelector[M, ID]

  type Selector = MSelector

  override def generateSelector(session: Session, id: ID) = MSelector(session, id)

  private val _all = scala.collection.mutable.ListBuffer[M]()

  override def all(implicit session: Session) = Future.successful { _all }

  override protected def addImpl(m: M)(implicit session: Session) = Future.successful {
    m.id = nextId
    _all += m
    m
  }

  def removeAll() = _all.clear()

  override def remove(selector: Selector) {
    findBySelector(selector).onSuccess {
      case Some(found) => _all -= found
      case None        => // do nothing
    }
  }

  override protected def updateImpl(m: M)(implicit session: Session) = Future.successful {
    m.id match {
      case Some(id) =>
        removeById(id)
        _all += m
        m
      case None => throw new Exception("ID required for update")
    }
  }

  override def findBySelector(selector: Selector) = Future.successful {
    _all.find(_.id.get == selector.id)
  }
}
