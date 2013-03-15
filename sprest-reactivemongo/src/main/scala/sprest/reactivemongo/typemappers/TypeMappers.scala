package sprest.reactivemongo.typemappers

import reactivemongo.bson._
import spray.json._

trait ValueTypeMapper[T] {
  def writeBSONValue(t: T): BSONValue
  def readBSONValue(v: BSONValue): T
}

object ValueTypeMapper {

  implicit object StringTypeMapper extends ValueTypeMapper[String] {
    def writeBSONValue(s: String) = BSONString(s)
    def readBSONValue(v: BSONValue) = v match {
      case BSONString(str) => str
      case _               => throw new Exception(s"Unexpected BSON value for deserialization")
    }
  }

  implicit object LongTypeMapper extends ValueTypeMapper[Long] {
    def writeBSONValue(l: Long) = BSONLong(l)
    def readBSONValue(v: BSONValue) = v match {
      case BSONLong(l) => l
      case _           => throw new Exception(s"Unexpected BSON value for deserialization")
    }
  }

  implicit object JsonTypeMapper extends ValueTypeMapper[JsValue] {
    def writeBSONValue(json: JsValue) = json match {
      case JsString(value) => BSONString(value)
      case JsNumber(num) =>
        if (num.isValidInt) BSONInteger(num.intValue)
        else if (num.isValidLong) BSONLong(num.longValue)
        else BSONDouble(num.doubleValue)
      case JsFalse          => BSONBoolean(false)
      case JsTrue           => BSONBoolean(true)
      case JsNull           => BSONNull
      case JsArray(elems)   => BSONArray(elems.map(writeBSONValue): _*)
      case JsObject(fields) => BSONDocument(fields.toList.map(entry => entry._1 -> writeBSONValue(entry._2)): _*)
    }

    def readBSONValue(bson: BSONValue) = bson match {
      case BSONString(value)     => JsString(value)
      case BSONDouble(value)     => JsNumber(value)
      case BSONInteger(value)    => JsNumber(value)
      case BSONLong(value)       => JsNumber(value)
      case BSONBoolean(value)    => JsBoolean(value)
      case BSONNull              => JsNull
      case arr: BSONArray        => JsArray(arr.values.map(readBSONValue).toList)
      case bsonDoc: BSONDocument => JsObject(bsonDoc.mapped.map(elem => elem._1 -> readBSONValue(elem._2)))
    }
  }

}

