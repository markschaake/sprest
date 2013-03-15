package sprest.reactivemongo

import spray.json._
import sprest.models.Model

trait ModelCompanion[M <: Model[ID], ID] extends DefaultJsonProtocol {
  import reactivemongo.bson.handlers.{ BSONReader, BSONWriter }
  import reactivemongo.bson._

  trait BSONFormat[T] extends BSONReader[T] with BSONWriter[T]

  import sprest.reactivemongo.typemappers.ValueTypeMapper.JsonTypeMapper

  def generateBSONFormat(jsonFormat: RootJsonFormat[M]): BSONFormat[M] = new BSONFormat[M] {
    def fromBSON(bson: BSONDocument): M = {
      val preIdFix = JsonTypeMapper.readBSONValue(bson).asJsObject
      jsonFormat.read(preIdFix.copy(fields = preIdFix.fields.map(elem => if (elem._1 == "_id") "id" -> elem._2 else elem)))
    }

    def toBSON(m: M): BSONDocument = {
      val json = {
        val js = jsonFormat.write(m).asJsObject
        js.fields.get("id") match {
          case Some(idValue) => js.copy(fields = js.fields.map(elem => if (elem._1 == "id") "_id" -> elem._2 else elem))
          case _             => js
        }
      }
      JsonTypeMapper.writeBSONValue(json).asInstanceOf[BSONDocument]
    }
  }

}

