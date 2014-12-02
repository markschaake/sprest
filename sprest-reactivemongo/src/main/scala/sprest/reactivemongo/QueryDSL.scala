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

  def $exists(fieldToExistence: (String, Boolean)): JsField =
    fieldToExistence._1 -> JsObject(
      "$exists" -> fieldToExistence._2.toJson)

  // Comparisons
  private[this] def comparison[V](operator: String, fieldNameToValue: (String, V))(implicit jsWriter: JsonWriter[V]): JsField =
    fieldNameToValue._1 -> JsObject(
      operator -> jsWriter.write(fieldNameToValue._2))

  def $gt[V](fieldNameToValue: (String, V))(implicit jsWriter: JsonWriter[V]): JsField = comparison("$gt", fieldNameToValue)
  def $gte[V](fieldNameToValue: (String, V))(implicit jsWriter: JsonWriter[V]): JsField = comparison("$gte", fieldNameToValue)
  def $lt[V](fieldNameToValue: (String, V))(implicit jsWriter: JsonWriter[V]): JsField = comparison("$lt", fieldNameToValue)
  def $lte[V](fieldNameToValue: (String, V))(implicit jsWriter: JsonWriter[V]): JsField = comparison("$lte", fieldNameToValue)
  def $ne[V](fieldNameToValue: (String, V))(implicit jsWriter: JsonWriter[V]): JsField = comparison("$ne", fieldNameToValue)

  // Array Comparisons
  private[this] def arrayComparison[V](operator: String, fieldNameToArray: (String, List[V]))(implicit jsWriter: JsonWriter[V]) =
    fieldNameToArray._1 -> JsObject(
      operator -> JsArray(fieldNameToArray._2.map(jsWriter.write(_)): _*))

  def $all[V](fieldNameToArray: (String, List[V]))(implicit jsWriter: JsonWriter[V]): JsField = arrayComparison("$all", fieldNameToArray)
  def $in[V](fieldNameToArray: (String, List[V]))(implicit jsWriter: JsonWriter[V]): JsField = arrayComparison("$in", fieldNameToArray)
  def $nin[V](fieldNameToArray: (String, List[V]))(implicit jsWriter: JsonWriter[V]): JsField = arrayComparison("$nin", fieldNameToArray)

  def $elemMatch(arrayField: String, matchExpressions: JsField*): JsField =
    arrayField -> JsObject("$elemMatch" -> JsObject(matchExpressions: _*))

  // Logical
  private[this] def logicalSequence(operator: String, expressions: List[JsObject]): JsField =
    operator -> JsArray(expressions: _*)

  def $or(expressions: List[JsField]): JsField = logicalSequence("$or", expressions)
  def $nor(expressions: List[JsField]): JsField = logicalSequence("$nor", expressions)
  def $and(expressions: List[JsField]): JsField = logicalSequence("$and", expressions)
  def $not(expression: JsObject) = "$not" -> expression

  // Update operators

  def $set[V](fieldToValue: (String, V))(implicit jsWriter: JsonWriter[V]): JsField =
    "$set" -> JsObject(
      fieldToValue._1 -> jsWriter.write(fieldToValue._2))

  def $unset(fieldName: String): JsField = "$unset" -> JsObject(fieldName -> "".toJson)

  def $rename(oldToNew: (String, String)*): JsField =
    "$rename" -> JsObject(
      oldToNew.map(tup => tup._1 -> tup._2.toJson).toList: _*)

  def $inc(fieldNameToAmount: (String, Int)): JsField =
    "$inc" -> JsObject(
      fieldNameToAmount._1 -> fieldNameToAmount._2.toJson)

}

