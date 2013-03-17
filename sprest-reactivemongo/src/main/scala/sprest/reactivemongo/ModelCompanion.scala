package sprest.reactivemongo

import spray.json._
import sprest.models.Model

trait ModelCompanion[M <: Model[ID], ID] extends DefaultJsonProtocol
