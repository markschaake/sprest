package sprest.reactivemongo

import sprest.models.Model
import sprest.models.ModelCompanion
import sprest.models.UniqueSelector
import sprest.reactivemongo.typemappers.SprayJsonTypeMapper

import org.specs2.mutable._
import org.specs2.specification.Scope
import reactivemongo.api._
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentWriter
import reactivemongo.core.commands.LastError
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

import typemappers._

class PersistenceSpec extends Specification {

  val driver = new MongoDriver
  lazy val connection = driver.connection(List("localhost"))
  lazy val mongoDB: reactivemongo.api.DefaultDB = connection("sprest-reactivemongo-test")

  trait MongoScope extends Scope with DefaultJsonProtocol with ReactiveMongoPersistence {

    def collection(collName: String) = mongoDB(collName)
    implicit object typeMapper extends SprayJsonTypeMapper with NormalizedIdTransformer

    val defaultDuration = Duration(2, SECONDS)

    def blockForResult[T](f: => Future[T]) = Await.result(f, defaultDuration)
    def blockForLastError(f: => Future[LastError]) = blockForResult(f)

    def findByIdQuery(id: String) = JsObject("_id" -> id.toJson)

    case class Foo(id: String, name: String, age: Int) extends Model[String]
    object Foo extends ModelCompanion[Foo, String] {
      implicit val jsFormat = jsonFormat3(Foo.apply _)
    }

    case class Bar(id: String, fullname: String, spec: Double) extends Model[String]
    object Bar extends ModelCompanion[Bar, String] {
      implicit val jsFormat = jsonFormat3(Bar.apply _)
    }

    case class SubFoo(name: String)
    object SubFoo {
      implicit val jsFormat = jsonFormat1(SubFoo.apply _)
      val barReads = new RootJsonReader[SubFoo] {
        override def read(jsValue: JsValue): SubFoo = jsValue match {
          case JsObject(fields) => SubFoo(fields("fullname").asInstanceOf[JsString].value)
          case _ => throw new SerializationException("required JsObject")
        }
      }
      implicit val fooProjection = Projection[Foo, SubFoo](JsObject("name" -> 1.toJson), jsFormat)
      implicit val barProjection = Projection[Bar, SubFoo](JsObject("fullname" -> 1.toJson), barReads)
    }

    abstract class BasicDAO[M <: Model[String]](collName: String)(implicit jsFormat: RootJsonFormat[M])
        extends CollectionDAO[M, String](collection(collName)) {
      case class TheSelector(id: String) extends UniqueSelector[M, String]
      type Selector = TheSelector

      def generateId = java.util.UUID.randomUUID.toString

      override protected def addImpl(m: M)(implicit ec: ExecutionContext) = doAdd(m)
      override protected def updateImpl(m: M)(implicit ec: ExecutionContext) = doUpdate(m)
      override def remove(selector: Selector)(implicit ec: ExecutionContext) = uncheckedRemoveById(selector.id)
      override implicit def generateSelector(id: String) = TheSelector(id)
    }

    class FooDAO(collName: String) extends BasicDAO[Foo](collName)
    class BarDAO(collName: String) extends BasicDAO[Bar](collName)

  }

  sequential

  "find" should {
    "sort ascending" in new MongoScope {
      val fooDao = new FooDAO("findAsc")
      val added1 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo1", age = 1)))
      val added2 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo2", age = 3)))
      val added3 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo3", age = 2)))
      val queryResult = blockForResult(fooDao.find(JsObject(), Sort("age" -> Sort.Asc)))
      queryResult(0).age must_== 1
      queryResult(1).age must_== 2
      queryResult(2).age must_== 3
    }

    "sort descending" in new MongoScope {
      val fooDao = new FooDAO("findDesc")
      val added1 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo1", age = 1)))
      val added2 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo2", age = 3)))
      val added3 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo3", age = 2)))
      val queryResult = blockForResult(fooDao.find(JsObject(), Sort("age" -> Sort.Desc)))
      queryResult(0).age must_== 3
      queryResult(1).age must_== 2
      queryResult(2).age must_== 1
    }

    "sort with multiple sorts" in new MongoScope {
      val fooDao = new FooDAO("findMultiSort")
      val added1 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "FooA", age = 1)))
      val added2 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "FooZ", age = 2)))
      val added3 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "FooH", age = 2)))
      val queryResult = blockForResult(fooDao.find(JsObject(), List(Sort("age" -> Sort.Desc), Sort("name" -> Sort.Asc))))
      queryResult(0).age must_== 2
      queryResult(0).name must_== "FooH"
      queryResult(1).age must_== 2
      queryResult(1).name must_== "FooZ"
      queryResult(2).age must_== 1
    }
  }

  "findAs" should {
    "project to another object with explicit projection" in new MongoScope {
      // do the insert first:
      val fooDao = new FooDAO("findAs")
      val added = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo", age = 123)))
      val projection = JsObject("name" -> 1.toJson, "_id" -> 0.toJson)
      val foundSubFoos = blockForResult(fooDao.findAs[SubFoo](findByIdQuery(added.id), projection))

      foundSubFoos must have size 1
      foundSubFoos.head must beAnInstanceOf[SubFoo]
      foundSubFoos.head.name must_== "Foo"
    }

    "project to another object with implicit projection" in new MongoScope {
      val fooDao = new FooDAO("findAs")
      val added = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo3", age = 12)))
      val foundSubFoos = blockForResult(fooDao.findAs[SubFoo](findByIdQuery(added.id)))

      foundSubFoos must have size 1
      foundSubFoos.head must beAnInstanceOf[SubFoo]
      foundSubFoos.head.name must_== "Foo3"
    }

    "project to another object with different projection implicitly" in new MongoScope {
      val barDao = new BarDAO("findAs")
      val added = blockForResult(barDao.add(Bar(id = barDao.generateId, fullname = "Big Guy", spec = 10.5)))
      val foundSubFoos = blockForResult(barDao.findAs[SubFoo](findByIdQuery(added.id)))
      foundSubFoos.head.name must_== "Big Guy"
    }
  }

  "findOneAs" should {
    "project to another object explicitly" in new MongoScope {
      // do the insert first:
      val fooDao = new FooDAO("findOneAs")
      val added = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo2", age = 1234)))
      val projection = JsObject("name" -> 1.toJson, "_id" -> 0.toJson)
      val foundSubFoo = blockForResult(fooDao.findOneAs[SubFoo](findByIdQuery(added.id), projection))

      foundSubFoo must beSome
      foundSubFoo.get.name must_== "Foo2"
    }

    "project to another object implicitly" in new MongoScope {
      // do the insert first:
      val fooDao = new FooDAO("findOneAs")
      val added = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo2", age = 1234)))
      val foundSubFoo = blockForResult(fooDao.findOneAs[SubFoo](findByIdQuery(added.id)))

      foundSubFoo must beSome
      foundSubFoo.get.name must_== "Foo2"
    }

    "project to another object with different projection implicitly" in new MongoScope {
      val barDao = new BarDAO("findOneAs")
      val added = blockForResult(barDao.add(Bar(id = barDao.generateId, fullname = "Big Guy 2", spec = 10.5)))
      val foundSubFoo = blockForResult(barDao.findOneAs[SubFoo](findByIdQuery(added.id)))
      foundSubFoo must beSome(SubFoo("Big Guy 2"))
    }
  }

  "count" should {
    "return count of all in a collection" in new MongoScope {
      val fooDao = new FooDAO("count")
      val added1 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo1", age = 1)))
      val added2 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo2", age = 3)))
      val added3 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo3", age = 3)))

      val count = blockForResult(fooDao.count())
      count must_== 3
    }

    "return count of result of a query" in new MongoScope {
      val fooDao = new FooDAO("countWithQuery")
      val added1 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo1", age = 1)))
      val added2 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo2", age = 3)))
      val added3 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo3", age = 3)))

      val count = blockForResult(fooDao.count(JsObject("age" -> 3.toJson)))
      count must_== 2
    }
  }

  "updateWhere" should {
    "update all models that match the query" in new MongoScope {
      val fooDao = new FooDAO("updateWhere")
      val added1 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo1", age = 1)))
      val added2 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo2", age = 3)))
      val added3 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo3", age = 3)))
      val query = JsObject("age" -> 3.toJson)
      val fields = JsObject("age" -> 4.toJson)
      val lastError = blockForResult(fooDao.updateWhere(query, fields))
      lastError.inError must_== false
      blockForResult(fooDao.find(query)) must have size 0
      blockForResult(fooDao.find(JsObject("age" -> 4.toJson))) must have size 2
    }
  }

  "updateById" should {
    "update fields specified" in new MongoScope {
      val fooDao = new FooDAO("updateById")
      val added = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo1", age = 1)))
      val fields = JsObject("age" -> 2.toJson)
      val updated = blockForResult(fooDao.updateById(added.id, fields))
      val retrieved = blockForResult(fooDao.findById(updated.id)).get
      retrieved.age must_== 2
    }
  }

  "deleteWhere" should {
    "delete all models that match the query" in new MongoScope {
      val fooDao = new FooDAO("deleteWhere")
      val added1 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo1", age = 1)))
      val added2 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo2", age = 3)))
      val added3 = blockForResult(fooDao.add(Foo(id = fooDao.generateId, name = "Foo3", age = 3)))
      val query = JsObject("age" -> 3.toJson)
      val lastError = blockForResult(fooDao.deleteWhere(query))
      lastError.inError must_== false
      blockForResult(fooDao.count(query)) must_== 0
      blockForResult(fooDao.count(JsObject("age" -> 1.toJson))) must_== 1
    }
  }

  step {
    println("dropping database")
    mongoDB.drop()
    connection.close()
  }
}
