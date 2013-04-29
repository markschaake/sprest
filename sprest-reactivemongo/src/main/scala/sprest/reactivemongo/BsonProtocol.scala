package sprest.reactivemongo

import spray.json._
import reactivemongo.bson._
import sprest.reactivemongo.typemappers.{ BSONTypeMapper, SprayJsonTypeMapper }

/**
 * Provides helpers for generating BSONReader / BSONWriter objects
 */
trait BsonProtocol extends DefaultBSONHandlers {

  trait BSONFormat[T] extends BSONDocumentReader[T] with BSONDocumentWriter[T]

  def generateBSONFormat[M](implicit typeMapper: SprayJsonTypeMapper, jsonFormat: RootJsonFormat[M]): BSONFormat[M] = new BSONFormat[M] {
    override def read(bson: BSONDocument): M = jsonFormat.read(typeMapper.fromBSON(bson))
    override def write(m: M): BSONDocument = typeMapper.toBSON(jsonFormat.write(m)).asInstanceOf[BSONDocument]
  }

}
