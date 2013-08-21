package sprest.reactivemongo

import spray.json.JsObject
import spray.json.RootJsonReader

/**
 * @tparam M the model from which to project
 * @tparam P the type to which to project
 */
trait Projection[M, P] {
  def projection: JsObject
  def reads: RootJsonReader[P]
}

object Projection {
  def apply[M, P](proj: JsObject, read: RootJsonReader[P]) = new Projection[M, P] {
    override val projection: JsObject = proj
    override val reads: RootJsonReader[P] = read
  }
}
