package sprest.reactivemongo.typemappers

import org.specs2.mutable._
import spray.json._
import reactivemongo.bson._
import ValueTypeMapper._

class TypeMappersSpec extends Specification {

  "JsonTypeMapper" should {
    "map JsString" in {
      val json = JsString("hi there")
      val bson = JsonTypeMapper.writeBSONValue(json)
      bson.asInstanceOf[BSONString].value must_== "hi there"
      JsonTypeMapper.readBSONValue(bson) must_== json
    }

    "map JsNumber to BSONInteger" in {
      val json = JsNumber(12)
      val bson = JsonTypeMapper.writeBSONValue(json)
      bson.asInstanceOf[BSONInteger].value must_== 12
      JsonTypeMapper.readBSONValue(bson) must_== json
    }

    "map JsNumber to BSONLong" in {
      val json = JsNumber(Long.MaxValue)
      val bson = JsonTypeMapper.writeBSONValue(json)
      bson.asInstanceOf[BSONLong].value must_== Long.MaxValue
      JsonTypeMapper.readBSONValue(bson) must_== json
    }

    "map JsNumber to BSONDouble" in {
      val json = JsNumber(12.3)
      val bson = JsonTypeMapper.writeBSONValue(json)
      bson.asInstanceOf[BSONDouble].value must_== 12.3
      JsonTypeMapper.readBSONValue(bson) must_== json
    }

    "map JsFalse to BSONBoolean(false)" in {
      val json = JsFalse
      val bson = JsonTypeMapper.writeBSONValue(json)
      bson.asInstanceOf[BSONBoolean].value must_== false
      JsonTypeMapper.readBSONValue(bson) must_== json
    }

    "map JsTrue to BSONBoolean(true)" in {
      val json = JsTrue
      val bson = JsonTypeMapper.writeBSONValue(json)
      bson.asInstanceOf[BSONBoolean].value must_== true
      JsonTypeMapper.readBSONValue(bson) must_== json
    }

    "map JsNull to BSONNull" in {
      val json = JsNull
      val bson = JsonTypeMapper.writeBSONValue(json)
      bson must_== BSONNull
      JsonTypeMapper.readBSONValue(bson) must_== json
    }

    "map JsArray to BSONArray" in {
      val json = JsArray(JsString("one"), JsNumber(123.4), JsNull)
      val bson = JsonTypeMapper.writeBSONValue(json)
      val bsonList = bson.asInstanceOf[BSONArray].values.toList
      bsonList must have size 3
      bsonList(0) must_== BSONString("one")
      bsonList(1) must_== BSONDouble(123.4)
      bsonList(2) must_== BSONNull
      JsonTypeMapper.readBSONValue(bson) must_== json
    }

    "map JsObject to BSONDocument" in {
      val json = JsObject(
        "str" -> JsString("one"),
        "dbl" -> JsNumber(123.4),
        "null" -> JsNull)
      val bson = JsonTypeMapper.writeBSONValue(json)
      val bsonMapped = bson.asInstanceOf[BSONDocument].mapped
      bsonMapped must have size 3
      bsonMapped("str") must_== BSONString("one")
      bsonMapped("dbl") must_== BSONDouble(123.4)
      bsonMapped("null") must_== BSONNull
      JsonTypeMapper.readBSONValue(bson) must_== json

    }
  }
}

