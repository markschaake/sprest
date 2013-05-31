package sprest.util.enum

import org.specs2.mutable.Specification
import spray.json._

class EnumSpec extends Specification {

  sealed abstract class FakeEnum(name: String) extends Enum[FakeEnum](name)
  object FakeEnum extends EnumCompanion[FakeEnum] {
    case object Value1 extends FakeEnum("value1")
    case object Value2 extends FakeEnum("value2")
    case object Value3 extends FakeEnum("value3")
    register(Value1, Value2, Value3)
  }

  "Enum" should {
    "have all" in {
      FakeEnum.all must have size 3
    }
    "serialize to / from JSON" in {
      val results =
        FakeEnum.all.map { industry =>
          val js = industry.toJson
          js.convertTo[FakeEnum] must_== industry
        }
      results.toList
    }

    "fetch by name" in {
      FakeEnum.withName("value2").name must_== "value2"
    }
  }
}
