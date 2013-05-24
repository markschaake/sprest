package sprest.reactivemongo

package object typemappers {

  import reactivemongo.bson._
  import spray.json._

  /** JsObject -> BSONDocument */
  implicit object JsObjectBSONDocumentWriter extends BSONDocumentWriter[JsObject] with SprayJsonTypeMapper {
    override def write(jsObj: JsObject): BSONDocument = toBSON(jsObj).asInstanceOf[BSONDocument]
  }

}
