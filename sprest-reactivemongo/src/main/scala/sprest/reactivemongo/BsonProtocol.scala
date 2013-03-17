package sprest.reactivemongo

import spray.json._
import reactivemongo.bson._
import reactivemongo.bson.handlers._
import sprest.reactivemongo.typemappers.{ BSONTypeMapper, SprayJsonTypeMapper }

/**
 * Provides helpers for generating BSONReader / BSONWriter objects
 */
trait BsonProtocol {

  trait BSONFormat[T] extends BSONReader[T] with BSONWriter[T]

  def generateBSONFormat[M](implicit typeMapper: SprayJsonTypeMapper, jsonFormat: RootJsonFormat[M]): BSONFormat[M] = new BSONFormat[M] {
    def fromBSON(bson: BSONDocument): M = jsonFormat.read(typeMapper.fromBSON(bson))
    def toBSON(m: M): BSONDocument = typeMapper.toBSON(jsonFormat.write(m)).asInstanceOf[BSONDocument]
  }

}
