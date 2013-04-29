package sprest.reactivemongo.typemappers

import reactivemongo.bson._
import spray.json._

trait BSONTypeMapper[T] extends ElementNameTransformer {
  type BSONElem = (String, BSONValue)
  def toBSON(t: T): BSONValue
  def fromBSON(v: BSONValue): T
}

object BSONTypeMapper {
  implicit object StringTypeMapper extends BSONTypeMapper[String] {
    def toBSON(s: String) = BSONString(s)
    def fromBSON(v: BSONValue) = v match {
      case BSONString(str) => str
      case _               => throw new Exception(s"Unexpected BSON value for deserialization")
    }
  }

  implicit object LongTypeMapper extends BSONTypeMapper[Long] {
    def toBSON(l: Long) = BSONLong(l)
    def fromBSON(v: BSONValue) = v match {
      case BSONLong(l) => l
      case _           => throw new Exception(s"Unexpected BSON value for deserialization")
    }
  }
}

trait ElementNameTransformer {
  def transformForBSON(name: String): String = name
  def transformFromBSON(name: String): String = name
}

/**
 * Stackable trait that transforms element names of "_id" to "id"
 */
trait NormalizedIdTransformer extends ElementNameTransformer {
  abstract override def transformForBSON(name: String): String =
    if (super.transformForBSON(name) == "id") "_id"
    else super.transformForBSON(name)
  abstract override def transformFromBSON(name: String): String =
    if (super.transformFromBSON(name) == "_id") "id"
    else super.transformFromBSON(name)
}

trait SprayJsonTypeMapper extends BSONTypeMapper[JsValue] {
  def toBSON(json: JsValue) = json match {
    case JsString(value) => BSONString(value)
    case JsNumber(num) =>
      if (num.isValidInt) BSONInteger(num.intValue)
      else if (num.isValidLong) BSONLong(num.longValue)
      else BSONDouble(num.doubleValue)
    case JsFalse          => BSONBoolean(false)
    case JsTrue           => BSONBoolean(true)
    case JsNull           => BSONNull
    case JsArray(elems)   => BSONArray(elems.map(toBSON))
    case JsObject(fields) => BSONDocument(fields.toList.map(entry => transformForBSON(entry._1) -> toBSON(entry._2)))
  }

  def fromBSON(bson: BSONValue) = bson match {
    case BSONString(value)     => JsString(value)
    case BSONDouble(value)     => JsNumber(value)
    case BSONInteger(value)    => JsNumber(value)
    case BSONLong(value)       => JsNumber(value)
    case BSONBoolean(value)    => JsBoolean(value)
    case BSONNull              => JsNull
    case arr: BSONArray        => JsArray(arr.values.map(fromBSON).toList)
    case bsonDoc: BSONDocument => JsObject(bsonDoc.elements.toList.map { (elem => transformFromBSON(elem._1) -> fromBSON(elem._2)) })
  }
}

