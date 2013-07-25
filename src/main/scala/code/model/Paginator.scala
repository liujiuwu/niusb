package code.model

import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.mapper._

case class PaginatorData[T](total: Long, datas: Seq[T], pageNo: Long = 1, itemsPerPage: Long = 20) {
  def totalPage = (total / itemsPerPage).toInt + (if (total % itemsPerPage > 0) 1 else 0)
}

trait Paginator[T] {
  def paginator(page: Long, itemsPerPage: Int, by: QueryParam[Brand]*): PaginatorData[T]
}

  
