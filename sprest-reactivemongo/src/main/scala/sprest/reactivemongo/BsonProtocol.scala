package sprest.reactivemongo

import spray.json._
import reactivemongo.bson._
import reactivemongo.bson.handlers._
import sprest.reactivemongo.typemappers.BSONTypeMapper

/**
 * Provides helpers for generating BSONReader / BSONWriter objects
 */
trait BsonProtocol {

  trait BSONFormat[T] extends BSONReader[T] with BSONWriter[T]

  def generateBSONFormat[M](typeMapper: BSONTypeMapper[M]): BSONFormat[M] = new BSONFormat[M] {
    def fromBSON(bson: BSONDocument): M = typeMapper.fromBSON(bson)
    def toBSON(m: M): BSONDocument = typeMapper.toBSON(m).asInstanceOf[BSONDocument]
  }

}
