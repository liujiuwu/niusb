package code.model

case class Paginator[T](total: Long, datas: Seq[T], pageNo: Long = 1, itemsPerPage: Long = 20) {
  def totalPage = (total / itemsPerPage).toInt + (if (total % itemsPerPage > 0) 1 else 0)
  
}