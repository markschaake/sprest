package sprest.models

import java.util.UUID

trait Model[ID] {
  var id: Id[ID]
}

trait DAO[M <: Model[ID], ID] {
  def add(m: M): M
  def update(m: M): M
  def remove(id: ID)
  def all: Iterable[M]
  def findById(id: ID): Option[M]
  protected def nextId: Option[ID]
}

trait IntId { this: DAO[_, Int] =>
  protected var lastId = 0
  protected def nextId = {
    lastId += 1
    Some(lastId)
  }
}

trait LongId { this: DAO[_, Long] =>
  protected var lastId = 0
  protected def nextId = {
    lastId += 1
    Some(lastId)
  }
}

trait UUIDStringId { this: DAO[_, String] =>
  protected def nextId = {
    Some(UUID.randomUUID.toString)
  }
}

trait UUIDId { this: DAO[_, UUID] =>
  protected def nextId = {
    Some(UUID.randomUUID)
  }
}

trait MutableListDAO[M <: Model[ID], ID] extends DAO[M, ID] {

  val all = scala.collection.mutable.ListBuffer[M]()

  def add(m: M) = {
    m.id = nextId
    all += m
    m
  }

  def remove(id: ID) = {
    findById(id) foreach { found =>
      all -= found
    }
  }

  def removeAll() = all.clear()

  def update(m: M) = {
    m.id match {
      case Some(id) =>
        remove(id)
        all += m
        m
      case None => throw new Exception("ID required for update")
    }
  }

  def findById(id: ID) = all.find(_.id.get == id)
}
