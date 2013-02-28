package sprest.slick

package object typemappers {

  object Implicits {
    implicit def date2dateTime = DateTimeMapper.date2dateTime
  }

}
