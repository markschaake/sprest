package sprest.slick.typemappers

import scala.slick.lifted.MappedTypeMapper
import java.sql.Date
import org.joda.time.DateTime
import slick.lifted.TypeMapper.DateTypeMapper
 
object DateTimeMapper {
 
  implicit def date2dateTime = MappedTypeMapper.base[DateTime, Date] (
    dateTime => new Date(dateTime.getMillis),
    date => new DateTime(date)
  )
 
}
