package sprest.models

import java.util.UUID

import spray.json._

trait Model[ID] {
  var id: Id[ID]
}

/**
 * Provides conveniences typically needed for Model companion objects, such as Json format helpers
 */
trait ModelCompanion[M <: Model[ID], ID] extends DefaultJsonProtocol

trait IntId { this: DAO[_, Int, _] =>
  protected var lastId = 0
  override protected def nextId: Option[Int] = {
    lastId += 1
    Some(lastId)
  }
}

trait LongId { this: DAO[_, Long, _] =>
  protected var lastId = 0
  override protected def nextId: Option[Long] = {
    lastId += 1
    Some(lastId)
  }
}

trait UUIDStringId { this: DAO[_, String, _] =>
  override protected def nextId: Option[String] = Some(UUID.randomUUID.toString)
}

trait UUIDId { this: DAO[_, UUID, _] =>
  override protected def nextId: Option[UUID] = Some(UUID.randomUUID)
}

