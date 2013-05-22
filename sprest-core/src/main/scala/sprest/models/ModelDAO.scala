package sprest.models

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import sprest.security.Session

trait UniqueSelector[M <: Model[ID], ID] {
  def id: ID
  def session: Option[Session]
}

trait DAO[M <: Model[ID], ID, SessionImpl <: Session] {

  /** An object used for selecting a unique record */
  type Selector <: UniqueSelector[M, ID]

  implicit def generateSelector(id: ID, maybeSession: Option[SessionImpl]): Selector

  final def add(m: M)(implicit maybeSession: Option[SessionImpl]): Future[M] = addImpl(prePersist(m)) map { postPersist }
  final def update(m: M)(implicit maybeSession: Option[SessionImpl]): Future[M] = updateImpl(prePersist(m)) map { postPersist }

  def remove(selector: Selector)
  def removeById(id: ID)(implicit maybeSession: Option[SessionImpl]) = remove(generateSelector(id, maybeSession))

  def all(implicit maybeSession: Option[SessionImpl]): Future[Iterable[M]]

  def findBySelector(selector: Selector): Future[Option[M]]

  def findById(id: ID)(implicit maybeSession: Option[SessionImpl]) = findBySelector(generateSelector(id, maybeSession))

  protected def nextId: Option[ID] = None

  protected def addImpl(m: M)(implicit maybeSession: Option[SessionImpl]): Future[M]
  protected def updateImpl(m: M)(implicit maybeSession: Option[SessionImpl]): Future[M]

  protected def prePersist(m: M)(implicit maybeSession: Option[SessionImpl]): M = m
  protected def postPersist(m: M)(implicit maybeSession: Option[SessionImpl]): M = m
}

trait MutableListDAO[M <: Model[ID], ID, SessionImpl <: Session] extends DAO[M, ID, SessionImpl] {

  case class MSelector(id: ID, session: Option[SessionImpl]) extends UniqueSelector[M, ID]

  type Selector = MSelector

  override def generateSelector(id: ID, maybeSession: Option[SessionImpl]) = MSelector(id, maybeSession)

  protected val _all = scala.collection.mutable.ListBuffer[M]()

  override def all(implicit maybeSession: Option[SessionImpl]) = Future.successful { _all.toIterable }

  override protected def addImpl(m: M)(implicit maybeSession: Option[SessionImpl]) = Future.successful {
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

  override protected def updateImpl(m: M)(implicit maybeSession: Option[SessionImpl]) = Future.successful {
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
