package sprest.reactivemongo.typemappers

import org.specs2.mutable._
import spray.json._
import reactivemongo.bson._

class TypeMappersSpec extends Specification {

  object DefaultJsonTypeMapper extends SprayJsonTypeMapper
  object NormalizedIdJsonTypeMapper extends SprayJsonTypeMapper with NormalizedIdTransformer

  "JsonTypeMapper" should {
    "map JsString" in {
      val json = JsString("hi there")
      val bson = DefaultJsonTypeMapper.toBSON(json)
      bson.asInstanceOf[BSONString].value must_== "hi there"
      DefaultJsonTypeMapper.fromBSON(bson) must_== json
    }

    "map JsNumber to BSONInteger" in {
      val json = JsNumber(12)
      val bson = DefaultJsonTypeMapper.toBSON(json)
      bson.asInstanceOf[BSONInteger].value must_== 12
      DefaultJsonTypeMapper.fromBSON(bson) must_== json
    }

    "map JsNumber to BSONLong" in {
      val json = JsNumber(Long.MaxValue)
      val bson = DefaultJsonTypeMapper.toBSON(json)
      bson.asInstanceOf[BSONLong].value must_== Long.MaxValue
      DefaultJsonTypeMapper.fromBSON(bson) must_== json
    }

    "map JsNumber to BSONDouble" in {
      val json = JsNumber(12.3)
      val bson = DefaultJsonTypeMapper.toBSON(json)
      bson.asInstanceOf[BSONDouble].value must_== 12.3
      DefaultJsonTypeMapper.fromBSON(bson) must_== json
    }

    "map JsFalse to BSONBoolean(false)" in {
      val json = JsFalse
      val bson = DefaultJsonTypeMapper.toBSON(json)
      bson.asInstanceOf[BSONBoolean].value must_== false
      DefaultJsonTypeMapper.fromBSON(bson) must_== json
    }

    "map JsTrue to BSONBoolean(true)" in {
      val json = JsTrue
      val bson = DefaultJsonTypeMapper.toBSON(json)
      bson.asInstanceOf[BSONBoolean].value must_== true
      DefaultJsonTypeMapper.fromBSON(bson) must_== json
    }

    "map JsNull to BSONNull" in {
      val json = JsNull
      val bson = DefaultJsonTypeMapper.toBSON(json)
      bson must_== BSONNull
      DefaultJsonTypeMapper.fromBSON(bson) must_== json
    }

    "map JsArray to BSONArray" in {
      val json = JsArray(JsString("one"), JsNumber(123.4), JsNull)
      val bson = DefaultJsonTypeMapper.toBSON(json)
      val bsonList = bson.asInstanceOf[BSONArray].values.toList
      bsonList must have size 3
      bsonList(0) must_== BSONString("one")
      bsonList(1) must_== BSONDouble(123.4)
      bsonList(2) must_== BSONNull
      DefaultJsonTypeMapper.fromBSON(bson) must_== json
    }

    "map JsObject to BSONDocument" in {
      val json = JsObject(
        "str" -> JsString("one"),
        "dbl" -> JsNumber(123.4),
        "null" -> JsNull,
        "nested" -> JsObject(
          "foo" -> JsString("bar")))
      val bson = DefaultJsonTypeMapper.toBSON(json)
      val bsonMapped = bson.asInstanceOf[BSONDocument].elements.toMap
      bsonMapped must have size 4
      bsonMapped("str") must_== BSONString("one")
      bsonMapped("dbl") must_== BSONDouble(123.4)
      bsonMapped("null") must_== BSONNull
      val nested = bsonMapped("nested").asInstanceOf[BSONDocument].elements.toMap
      nested must have size 1
      nested("foo") must_== BSONString("bar")
      DefaultJsonTypeMapper.fromBSON(bson) must_== json

    }

    "retain _id field name in Json" in {
      val json = JsObject(
        "_id" -> JsString("one"),
        "dbl" -> JsNumber(123.4),
        "null" -> JsNull,
        "nested" -> JsObject(
          "foo" -> JsString("bar")))
      val bson = DefaultJsonTypeMapper.toBSON(json)
      val bsonMapped = bson.asInstanceOf[BSONDocument].elements.toMap
      bsonMapped must have size 4
      bsonMapped("_id") must_== BSONString("one")
      bsonMapped("dbl") must_== BSONDouble(123.4)
      bsonMapped("null") must_== BSONNull
      val nested = bsonMapped("nested").asInstanceOf[BSONDocument].elements.toMap
      nested must have size 1
      nested("foo") must_== BSONString("bar")
      DefaultJsonTypeMapper.fromBSON(bson) must_== json
    }

    "normalize _id field name in Json when NormalizedId trait mixed in" in {
      val json = JsObject(
        "id" -> JsString("one"),
        "dbl" -> JsNumber(123.4),
        "null" -> JsNull,
        "nested" -> JsObject(
          "foo" -> JsString("bar")))
      val bson = NormalizedIdJsonTypeMapper.toBSON(json)
      val bsonMapped = bson.asInstanceOf[BSONDocument].elements.toMap
      bsonMapped must have size 4
      bsonMapped("_id") must_== BSONString("one")
      bsonMapped("dbl") must_== BSONDouble(123.4)
      bsonMapped("null") must_== BSONNull
      val nested = bsonMapped("nested").asInstanceOf[BSONDocument].elements.toMap
      nested must have size 1
      nested("foo") must_== BSONString("bar")
      NormalizedIdJsonTypeMapper.fromBSON(bson) must_== json
    }
  }
}

