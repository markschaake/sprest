package sprest.reactivemongo.typemappers

import reactivemongo.bson._

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

}

