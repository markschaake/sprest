package sprest.util

package enum {

  import scala.collection.mutable.{ Map => MMap }
  import spray.json._

  abstract class Enum[T <: Enum[T]](val name: String, companion: EnumCompanion[T]) {
    companion._all(name) = this.asInstanceOf[T]
  }

  abstract class EnumCompanion[T <: Enum[T]] {

    private[enum] val _all = MMap[String, T]()

    implicit object jsonFormat extends RootJsonFormat[T] {
      override def write(t: T) = JsString(t.name)
      override def read(jsValue: JsValue) = jsValue match {
        case JsString(name) => _all(name)
        case _              => throw new SerializationException("Enum must be represented by JsString")
      }
    }

    def withName(name: String): T = _all(name)

    def all = _all.values

  }

}

