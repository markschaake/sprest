package sprest.reactivemongo

import spray.json._
import sprest.Formats._
import scala.collection.mutable
import scala.languageFeature.implicitConversions

object QueryDSL {

  implicit def expr[V](fieldNameToValue: (String, V))(implicit jsWriter: JsonWriter[V]): JsField =
    fieldNameToValue._1 -> jsWriter.write(fieldNameToValue._2)

  implicit def jsFieldToJsObject(jsField: JsField): JsObject = JsObject(jsField)

  implicit def jsFieldListToJsObjectList(jsFields: List[JsField]): List[JsObject] = jsFields map { jsFieldToJsObject }

  // Query operators

  def $exists(fieldToExistence: (String, Boolean)) =
    JsObject(
      fieldToExistence._1 -> JsObject(
        "$exists" -> fieldToExistence._2.toJson))

  // Comparisons
  private[this] def comparison[V](operator: String, fieldNameToValue: (String, V))(implicit jsWriter: JsonWriter[V]) =
    fieldNameToValue._1 -> JsObject(
      operator -> jsWriter.write(fieldNameToValue._2))

  def $gt[V](fieldNameToValue: (String, V))(implicit jsWriter: JsonWriter[V]) = comparison("$gt", fieldNameToValue)
  def $gte[V](fieldNameToValue: (String, V))(implicit jsWriter: JsonWriter[V]) = comparison("$gte", fieldNameToValue)
  def $lt[V](fieldNameToValue: (String, V))(implicit jsWriter: JsonWriter[V]) = comparison("$lt", fieldNameToValue)
  def $lte[V](fieldNameToValue: (String, V))(implicit jsWriter: JsonWriter[V]) = comparison("$lte", fieldNameToValue)
  def $ne[V](fieldNameToValue: (String, V))(implicit jsWriter: JsonWriter[V]) = comparison("$ne", fieldNameToValue)

  // Array Comparisons
  private[this] def arrayComparison[V](operator: String, fieldNameToArray: (String, List[V]))(implicit jsWriter: JsonWriter[V]) =
    fieldNameToArray._1 -> JsObject(
      operator -> JsArray(fieldNameToArray._2.map(jsWriter.write(_))))

  def $all[V](fieldNameToArray: (String, List[V]))(implicit jsWriter: JsonWriter[V]) = arrayComparison("$all", fieldNameToArray)
  def $in[V](fieldNameToArray: (String, List[V]))(implicit jsWriter: JsonWriter[V]) = arrayComparison("$in", fieldNameToArray)
  def $nin[V](fieldNameToArray: (String, List[V]))(implicit jsWriter: JsonWriter[V]) = arrayComparison("$nin", fieldNameToArray)

  def $elemMatch(arrayField: String, matchExpressions: JsField*) =
    arrayField -> JsObject("$elemMatch" -> JsObject(matchExpressions: _*))

  // Logical
  private[this] def logicalSequence(operator: String, expressions: List[JsObject]) =
    operator -> JsArray(expressions)

  def $or(expressions: List[JsField]) = logicalSequence("$or", expressions)
  def $nor(expressions: List[JsField]) = logicalSequence("$nor", expressions)
  def $and(expressions: List[JsField]) = logicalSequence("$and", expressions)
  def $not(expression: JsObject) = "$not" -> expression

  // Update operators

  def $set[V](fieldToValue: (String, V))(implicit jsWriter: JsonWriter[V]) =
    JsObject(
      "$set" -> JsObject(
        fieldToValue._1 -> jsWriter.write(fieldToValue._2)))

  def $unset(fieldName: String) = JsObject("$unset" -> JsObject(fieldName -> "".toJson))

  def $rename(oldToNew: (String, String)*) = JsObject(
    "$rename" -> JsObject(
      oldToNew.map(tup => tup._1 -> tup._2.toJson).toList))

  def $inc(fieldNameToAmount: (String, Int)) = JsObject(
    "$inc" -> JsObject(
      fieldNameToAmount._1 -> fieldNameToAmount._2.toJson))

}

