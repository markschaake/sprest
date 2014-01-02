package sprest.reactivemongo

import org.specs2.mutable._
import spray.json._
import spray.json.DefaultJsonProtocol._

class QueryDSLSpec extends Specification {

  import QueryDSL._

  "$exists" should {
    "generate json" in {
      $exists("foo" -> false) must_== "foo" -> JsObject("$exists" -> JsFalse)
    }
  }

  "$elemMatch" should {
    "generate json" in {
      $elemMatch("arr", expr("foo" -> false), expr("bar" -> 123), $gt("baz" -> 1)) must_==
        "arr" -> JsObject(
          "$elemMatch" -> JsObject(
            "foo" -> JsFalse,
            "bar" -> JsNumber(123),
            "baz" -> JsObject("$gt" -> JsNumber(1))))
    }
  }

}
