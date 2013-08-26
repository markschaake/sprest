package sprest.examples.slick

import org.joda.time.DateTime
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import sprest.models.Model
import sprest.slick.SlickPersistence
import sprest.Formats._
import spray.json.RootJsonFormat

trait DB extends SlickPersistence {

  import sprest.models.UniqueSelector
  import models._
  import com.typesafe.config.ConfigFactory

  object DBConfig {
    private val conf = ConfigFactory.load()
    def get(key: String): Option[String] = {
      // Note that any -D command-line arguments take precedence over application.conf settings
      // If you are using sbt-revolver, you can do this in the SBT console:
      // > re-start --- -Ddb.vendor=mysql -Ddb.user=me -Ddb.password=secret -Ddb.url="jdbc:..."
      // Alternatively, you can just modify application.conf
      Option(conf.getString(key))
    }

    lazy val vendor = get("db.vendor")
    lazy val url = get("db.url").getOrElse { throw new Exception("No database url configured") }
    lazy val user = get("db.user")
    lazy val pass = get("db.password")

    override def toString =
      s"DBConfig(\n\tvendor: $vendor\n\turl: $url\n\tuser: $user\n\tpassword: $pass\n)"

    println(toString)
  }

  override val backend = new SlickBackend {

    override val driver = DBConfig.vendor match {
      case Some("postgres") => scala.slick.driver.PostgresDriver
      case Some("mysql")    => scala.slick.driver.MySQLDriver
      case Some("sqlite")   => scala.slick.driver.SQLiteDriver
      case Some("h2")       => scala.slick.driver.H2Driver
      case None             => scala.slick.driver.H2Driver
      case Some(vendor)     => throw new Exception(s"Don't know how to deal with vendor [$vendor]")
    }

    override val database =
      if (DBConfig.user.isDefined && DBConfig.pass.isDefined) {
        driver.simple.Database.forURL(DBConfig.url, user = DBConfig.user.get, password = DBConfig.pass.get)
      } else {
        driver.simple.Database.forURL(DBConfig.url)
      }

  }

  import backend.driver.simple._

  object tables {

    object ToDoTable extends ModelTable[ToDo, Int]("todos") {
      def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
      def text = column[String]("text")
      def isComplete = column[Boolean]("is_complete")

      def * = text ~ isComplete ~ id.? <> (ToDo.apply _, ToDo.unapply _)

      // Let the DB generate the ID and return it on insert
      def ins = text ~ isComplete returning id
    }

    object ReminderTable extends ModelTable[Reminder, Int]("reminders") {
      import sprest.slick.typemappers.Implicits._

      def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
      def remindAt = column[DateTime]("remind_at")
      def title = column[String]("title")
      def body = column[Option[String]]("body")

      def * = remindAt ~ title ~ body ~ id.? <> (Reminder.apply _, Reminder.unapply _)
      def ins = remindAt ~ title ~ body returning id
    }

  }

  trait ExTableDAO[M <: Model[Int]] extends TableDAO[M, Int] {
    case class Selector(id: Int) extends UniqueSelector[M, Int]
    override def generateSelector(id: Int) = Selector(id)
  }

  // Sprest DAO
  object ToDos extends ExTableDAO[ToDo] {
    val table = tables.ToDoTable
    override protected def addImpl(m: ToDo)(implicit ec: ExecutionContext): Future[ToDo] = futureWithSession { implicit sess =>
      val id = table.ins.insert(m.text, m.done)
      m.id = Some(id)
      m
    }
  }

  object Reminders extends ExTableDAO[Reminder] {
    val table = tables.ReminderTable
    override protected def addImpl(m: Reminder)(implicit ec: ExecutionContext): Future[Reminder] = futureWithSession { implicit sess =>
      val id = table.ins.insert(m.remindAt, m.title, m.body)
      m.id = Some(id)
      m
    }
  }

}

