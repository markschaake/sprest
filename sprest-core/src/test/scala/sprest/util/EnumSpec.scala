package sprest.util.enum

import org.specs2.mutable.Specification
import spray.json._

class EnumSpec extends Specification {

  sealed abstract class FakeEnum(name: String) extends Enum[FakeEnum](name, FakeEnum)
  object FakeEnum extends EnumCompanion[FakeEnum] {
    case object Value1 extends FakeEnum("value1")
    case object Value2 extends FakeEnum("value2")
    case object Value3 extends FakeEnum("value3")
  }

  "Enum" should {
    "serialize to / from JSON" in {
      val results =
        FakeEnum.all.map { industry =>
          val js = industry.toJson
          js.convertTo[FakeEnum] must_== industry
        }
      results.toList
    }

    "fetch by name" in {
      FakeEnum.withName("value2") must_== FakeEnum.Value2
    }
  }
}
