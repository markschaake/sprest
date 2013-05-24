package sprest.reactivemongo

package object typemappers {

  import reactivemongo.bson._
  import spray.json.JsObject

  /** JsObject -> BSONDocument */
  implicit def jsObjectBSONDocumentWriter(implicit typeMapper: SprayJsonTypeMapper):BSONDocumentWriter[JsObject] =
    new BSONDocumentWriter[JsObject] {
      override def write(jsObj: JsObject): BSONDocument = BSONDocument(jsObj.fields.toList.map(entry => typeMapper.transformForBSON(entry._1) -> typeMapper.toBSON(entry._2)))
    }

}
