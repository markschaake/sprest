package sprest.reactivemongo

package object typemappers {

  import reactivemongo.bson._
  import spray.json.JsObject
  import spray.json.RootJsonReader

  /** JsObject -> BSONDocument */
  implicit def jsObjectBSONDocumentWriter(implicit typeMapper: SprayJsonTypeMapper): BSONDocumentWriter[JsObject] =
    new BSONDocumentWriter[JsObject] {
      override def write(jsObj: JsObject): BSONDocument = BSONDocument(jsObj.fields.toList.map(entry => typeMapper.transformForBSON(entry._1) -> typeMapper.toBSON(entry._2)))
    }

  implicit def jsObjectBSONDocumentReader[P](implicit typeMapper: SprayJsonTypeMapper, reads: RootJsonReader[P]): BSONDocumentReader[P] =
    new BSONDocumentReader[P] {
      override def read(bson: BSONDocument): P = {
        reads.read(typeMapper.fromBSON(bson).asJsObject)
      }
    }

  implicit def pimpedReader[T](reads: RootJsonReader[T])(implicit typeMapper: SprayJsonTypeMapper) = new BSONDocumentReader[T] {
    override def read(bson: BSONDocument): T = {
      reads.read(typeMapper.fromBSON(bson).asJsObject)
    }
  }

}
