package sprest.slick

import sprest.models._
import sprest.security.{ Session => SprestSession }
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait SlickPersistence {
  import scala.slick.driver.ExtendedDriver
  import scala.slick.session.{ Session => SlickSession, Database }
  import scala.slick.lifted.TypeMapper

  val backend: SlickBackend

  trait SlickBackend {
    val database: Database
    val driver: ExtendedDriver
  }

  import backend.driver.simple._

  def withSession[T](f: SlickSession => T) = backend.database withSession { implicit sess => f(sess) }

  def futureWithSession[T](f: SlickSession => T) = Future.successful { withSession(f) }

  abstract class ModelTable[M <: Model[ID], ID](name: String)(implicit tm: TypeMapper[ID]) extends Table[M](name) {
    def id: Column[ID]
    def byId = createFinderBy(_.id)
  }

  trait TableDAO[M <: Model[ID], ID, SessionImpl <: SprestSession] extends DAO[M, ID, SessionImpl] {
    def table: ModelTable[M, ID]

    override protected def allImpl(implicit maybeSession: Option[SessionImpl]): Future[Iterable[M]] = futureWithSession { implicit s => Query(table).list }

    override def findBySelector(selector: Selector): Future[Option[M]] = futureWithSession { implicit s => table.byId(selector.id).firstOption }

    override def remove(selector: Selector) = withSession { implicit s => table.byId(selector.id).mutate(_.delete) }

    override protected def updateImpl(m: M)(implicit maybeSession: Option[SessionImpl]) = futureWithSession { implicit s =>
      table.byId(m.id.get).mutate(_.row = m)
      m
    }

  }

  trait AutoIncrementingId { this: ModelTable[_, Int] =>
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  }

}
