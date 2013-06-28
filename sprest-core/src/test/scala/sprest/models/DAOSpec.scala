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
      val one = intDAO.findById(1)
      Await.ready(one, Duration(500, MILLISECONDS))
      println(one.value)
      one.value.get.get.get.foo must_== Some("1 ok")
    }

    "perform postFetch on all" in new DAOContext {
      val all = intDAO.all
      Await.ready(all, Duration(500, MILLISECONDS))
      val values = all.value.get.get
      values must have size 3
      values map { value =>
        value.foo must_== Some(value.id.get + " ok")
      }
    }

    "perform postFetch on add" in new DAOContext {
      val m = IntModel(
        id = None,
        name = "hi",
        userId = "first")
      val addedFuture = intDAO.add(m)
      Await.ready(addedFuture, Duration(500, MILLISECONDS))
      val added = addedFuture.value.get.get
      added.foo must not beNone
    }

    "perform postFetch on update" in new DAOContext {
      val m = IntModel(
        id = None,
        name = "hi",
        userId = "first")

      val future =
        for {
          added <- intDAO.add(m)
          updated <- intDAO.update(added.copy(name = "there"))
        } yield updated

      Await.ready(future, Duration(500, MILLISECONDS))
      val updated = future.value.get.get
      updated.foo must not beNone
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

  class IntDAO extends DAO[IntModel, Int]
    with MutableListDAO[IntModel, Int]
    with IntId {

    override protected def postFetch(m: IntModel)(implicit ec: ExecutionContext) = {
      m.foo = Some(m.id.get + " ok")
      Future.successful(m)
    }
  }

  trait DAOContext extends Scope {
    val intDAO = new IntDAO
    intDAO.add(IntModel(None, "first", "first"))
    intDAO.add(IntModel(None, "second", "second"))
    intDAO.add(IntModel(None, "anotherfirst", "first"))
  }

}
