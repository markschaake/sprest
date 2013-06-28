package sprest.routing

import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import scala.parallel.Future
import spray.http.StatusCodes
import spray.routing._
import shapeless._
import spray.routing.PathMatcher
import spray.json._
import spray.httpx.SprayJsonSupport._
import sprest.models._
import spray.json.DefaultJsonProtocol._
import java.util.UUID
import scala.util.{ Success, Failure }
import sprest.security.Session

/**
 * Mixin that provides helpers for generating RESTful routes.
 * Currently, limited to SprayJSON marshalling.
 */
trait RestRoutes { this: HttpService =>

  val system: ActorSystem

  // Import the exection context:
  import system.dispatcher

  /** Custom PathMatcher for dealing with UUID's stored as String (and not as UUID objects) */
  val JavaUUIDString: PathMatcher[java.lang.String :: HNil] = JavaUUID.map(foo => foo.toString :: HNil)

  type SessionImpl <: Session

  def maybeSession: Directive[Option[SessionImpl] :: HNil]

  /**
   * Directive to generate REST routes for the given model with id of type T
   * @tparam M Model with id of type T
   * @tparam T id type T
   * @param route prefix name, e.g. "users"
   * @param dao for model M
   * @param idMatcher PathMatcher that extracts type T from route
   */
  def rest[M <: Model[T], T](name: String, dao: DAO[M, T], idMatcher: PathMatcher[T :: HNil])(implicit marshaller: RootJsonFormat[M]) = {
    path(name) {
      maybeSession { implicit session =>
        get {
          complete(dao.all)
        } ~
          post {
            entity(as[M]) { m =>
              complete(dao.add(m))
            }
          }
      }
    } ~
      path(name / idMatcher) { id =>
        maybeSession { implicit session =>
          get { ctx =>
            dao.findById(id) map {
              case Some(m) => ctx.complete(m)
              case None    => ctx.complete(StatusCodes.NotFound)
            }
          } ~
            (put | post) {
              entity(as[M]) { m =>
                complete(dao.update(m))
              }
            } ~
            delete { ctx =>
              dao.findById(id) map {
                case Some(m) => ctx.complete {
                  dao.removeById(id)
                  StatusCodes.OK
                }
                case None => ctx.complete(StatusCodes.NotFound)
              }
            }
        }
      }
  }

  /**
   * Generates REST routes with Long id
   */
  def restLong[M <: Model[Long]](name: String, dao: DAO[M, Long])(implicit marshaller: RootJsonFormat[M]) =
    rest[M, Long](name, dao, LongNumber)

  /**
   * Generates REST routes with Int id
   */
  def restInt[M <: Model[Int]](name: String, dao: DAO[M, Int])(implicit marshaller: RootJsonFormat[M]) =
    rest[M, Int](name, dao, IntNumber)

  /**
   * Generates REST routes with java.util.UUID id
   */
  def restUUID[M <: Model[UUID]](name: String, dao: DAO[M, UUID])(implicit marshaller: RootJsonFormat[M]) =
    rest[M, UUID](name, dao, JavaUUID)

  /**
   * Generates REST routes with String UUID
   */
  def restUUIDString[M <: Model[String]](name: String, dao: DAO[M, String])(implicit marshaller: RootJsonFormat[M]) =
    rest[M, String](name, dao, JavaUUIDString)

  def restString[M <: Model[String]](name: String, dao: DAO[M, String])(implicit marshaller: RootJsonFormat[M]) =
    rest[M, String](name, dao, Segment)

}
