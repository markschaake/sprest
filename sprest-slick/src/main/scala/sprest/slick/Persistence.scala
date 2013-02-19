package sprest.slick

import sprest.models._

trait SlickPersistence {
  import scala.slick.driver.ExtendedDriver
  import scala.slick.session.Database

  val backend: SlickBackend

  trait SlickBackend {
    val database: Database
    val driver: ExtendedDriver
  }

  import backend.driver.simple._

  def withSession[T](f: scala.slick.session.Session => T) = backend.database withSession { implicit sess => f(sess) }

  abstract class ModelTable[M <: Model[ID], ID](name: String)(implicit tm: scala.slick.lifted.TypeMapper[ID])
      extends Table[M](name) {
     
    def id: Column[ID]
    def byId = createFinderBy(_.id)
  }

  trait TableDAO[M <: Model[ID], ID] extends DAO[M, ID] {
    def table: ModelTable[M, ID]
    
    def all: Iterable[M] = withSession { implicit s => Query(table).list }
    def findById(id: ID): Option[M] = withSession { implicit s =>  table.byId(id).firstOption }
    def remove(id: ID) = withSession { implicit s => table.byId(id).mutate(_.delete) }
    def update(m: M) = withSession { implicit s => 
      table.byId(m.id.get).mutate(_.row = m)
      m
    }
  }

}
