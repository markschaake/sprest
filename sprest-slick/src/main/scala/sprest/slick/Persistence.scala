package sprest.slick

import sprest.models._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait SlickPersistence {
  import scala.slick.driver.ExtendedDriver
  import scala.slick.session.{Session, Database}
  import scala.slick.lifted.TypeMapper

  val backend: SlickBackend

  trait SlickBackend {
    val database: Database
    val driver: ExtendedDriver
  }

  import backend.driver.simple._

  def withSession[T](f: Session => T) = backend.database withSession { implicit sess => f(sess) }

  def futureWithSession[T](f: Session => T) = Future.successful { withSession(f) }

  abstract class ModelTable[M <: Model[ID], ID](name: String)(implicit tm: TypeMapper[ID]) extends Table[M](name) {
    def id: Column[ID]
    def byId = createFinderBy(_.id)
  }

  trait TableDAO[M <: Model[ID], ID] extends DAO[M, ID] {
    def table: ModelTable[M, ID]
    def all: Future[Iterable[M]] = futureWithSession { implicit s => Query(table).list }
    def findById(id: ID): Future[Option[M]] = futureWithSession { implicit s =>  table.byId(id).firstOption }
    def remove(id: ID) = withSession { implicit s => table.byId(id).mutate(_.delete) }
    def update(m: M) = futureWithSession { implicit s =>
      table.byId(m.id.get).mutate(_.row = m)
      m
    }
  }

}
