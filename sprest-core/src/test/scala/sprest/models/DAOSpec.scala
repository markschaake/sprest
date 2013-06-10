package sprest.models

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import sprest.security.Session
import sprest.security.User
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class DAOSpec extends Specification {

  "DAO" should {
    "perform postFetch on findById" in new DAOContext {
      val one = intDAO.findById(1)(maybeMockSession, global)
      Await.ready(one, Duration(500, MILLISECONDS))
      println(one.value)
      one.value.get.get.get.foo must_== Some("1 ok")
    }

    "perform postFetch on all" in new DAOContext {
      val all = intDAO.all(maybeMockSession, global)
      Await.ready(all, Duration(500, MILLISECONDS))
      val values = all.value.get.get
      values must have size 2
      values map { value =>
        value.foo must_== Some(value.id.get + " ok")
      }
    }
  }

  case class MockUser(userId: String) extends User {
    type ID = String
  }

  case class MockSession(sessionId: String, user: MockUser) extends Session {
    type ID = String
  }

  case class IntModel(var id: Option[Int], name: String, userId: String) extends Model[Int] {
    var foo: Option[String] = None
    override def toString = {
      s"foo: $foo"
    }
  }

  class IntDAO extends DAO[IntModel, Int, MockSession]
    with MutableListDAO[IntModel, Int, MockSession]
    with IntId {

    override protected def postFetch(m: IntModel)(implicit maybeSession: Option[MockSession], ec: ExecutionContext) = {
      m.foo = Some(m.id.get + " ok")
      Future.successful(m)
    }

    override protected def addImpl(m: IntModel)(implicit maybeSession: Option[MockSession], ec: ExecutionContext) = maybeSession match {
      case Some(sess) if sess.user.userId == m.userId => Future.successful {
        m.id = nextId
        _all += m
        m
      }
      case Some(sess) => throw new Exception("Cannot create a model instance for another user")
      case None       => throw new Exception("Session required to add!")
    }

    override protected def allImpl(implicit maybeSession: Option[MockSession], ec: ExecutionContext): Future[Iterable[IntModel]] = maybeSession match {
      case Some(sess) => Future.successful {
        _all.filter(_.userId == sess.user.userId).toIterable
      }
      case None => Future.failed { new Exception("Session required!") }
    }
  }

  trait DAOContext extends Scope {
    val intDAO = new IntDAO
    val mockSession = MockSession("abcds", MockUser("first"))
    implicit val maybeMockSession = Some(mockSession)
    intDAO.add(IntModel(None, "first", "first"))
    intDAO.add(IntModel(None, "second", "second"))(Some(MockSession("efg", MockUser("second"))), global)
    intDAO.add(IntModel(None, "anotherfirst", "first"))
  }

}
