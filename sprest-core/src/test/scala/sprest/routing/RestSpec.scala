package sprest.routing

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import spray.testkit.Specs2RouteTest
import spray.routing.HttpService
import spray.http.StatusCodes._
import sprest.models._
import spray.json._

import sprest.security._

class RestSpec extends Specification
  with Specs2RouteTest
  with HttpService
  with RestRoutes
  with spray.httpx.SprayJsonSupport
  with DefaultJsonProtocol {

  case class MockUser(userId: String) extends User {
    type ID = String
  }

  case class MockSession(sessionId: String, user: MockUser) extends Session {
    type ID = String
  }

  implicit val mockSession = MockSession("abcdefg", MockUser("abcd"))

  override def withSession = provide(mockSession)

  def actorRefFactory = system

  case class IntModel(var id: Option[Int], name: String) extends Model[Int]
  implicit val IntModelFormat = jsonFormat2(IntModel)
  class IntDAO extends DAO[IntModel, Int]
    with MutableListDAO[IntModel, Int]
    with IntId

  "REST routes" should {
    "GET returns all" in new RoutesContext {
      Get("/ints") ~> intRoutes ~> check {
        entityAs[List[IntModel]] must have size 2
      }
    }

    "POST adds" in new RoutesContext {
      Post("/ints", IntModel(None, "third")) ~> intRoutes ~> check {
        val model = entityAs[IntModel]
        model.id must_== Some(3)
        model.name must_== "third"
      }
    }

    "GET by id returns single model" in new RoutesContext {
      Get("/ints/2") ~> intRoutes ~> check {
        entityAs[IntModel].name must_== "second"
      }
    }

    "GET by id does not exists responds with NotFound" in new RoutesContext {
      Get("/ints/3") ~> intRoutes ~> check {
        status === NotFound
      }
    }

    "PUT by id updates model" in new RoutesContext {
      Put("/ints/2", IntModel(Some(2), "2nd")) ~> intRoutes ~> check {
        entityAs[IntModel].name must_== "2nd"
      }
    }

    "DELETE by id removes model" in new RoutesContext {
      Get("/ints") ~> intRoutes ~> check {
        entityAs[List[IntModel]] must have size 2
        Delete("/ints/2") ~> intRoutes ~> check {
          Get("/ints") ~> intRoutes ~> check {
            entityAs[List[IntModel]] must have size 1
          }
        }
      }
    }
  }

  trait RoutesContext extends Scope {

    val intDAO = new IntDAO
    val intRoutes = restInt("ints", intDAO)

    intDAO.add(IntModel(None, "first"))
    intDAO.add(IntModel(None, "second"))
  }
}
