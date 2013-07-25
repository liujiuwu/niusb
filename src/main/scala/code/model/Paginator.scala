package code.model

import scala.xml._
import net.liftweb.mapper._
import net.liftweb.util.Helpers._
import net.liftweb.util.IterableConst._
import net.liftweb.util.NodeSeqIterableConst

case class PaginatorModel[T](total: Long, datas: Seq[T], currentPageNo: Long, itemsPerPage: Int = 20) {
  def totalPage = (total / itemsPerPage).toInt + (if (total % itemsPerPage > 0) 1 else 0)

  def pageXml(pageNo: Long, ns: NodeSeq): NodeSeq = {
    if (currentPageNo == pageNo || pageNo < 0 || pageNo > totalPage) {
      <span class="current">{ ns }</span>
    } else {
      <a href={ "/admin/brand/?page=" + pageNo + "&type=25" } class="page-link">{ ns }</a>
    }
  }

  def prevXml: NodeSeq = Text("上一页")
  def nextXml: NodeSeq = Text("下一页")
  def firstXml: NodeSeq = Text("首页")
  def lastXml: NodeSeq = Text("尾页")

  def recordsFrom: String = (currentPageNo * itemsPerPage min total) toString
  def recordsTo: String = ((currentPageNo * itemsPerPage + itemsPerPage) min total) toString
  def currentXml: NodeSeq =
    if (total == 0)
      Text(("paginator.norecords"))
    else
      Text(s"${recordsFrom} - ${recordsTo} of ${total}")

  def interval(totalPage: Int, currentPage: Long, displayedPages: Int = 5): (Int, Int) = {
    val realCurrentPage = if (currentPage > totalPage) 0 else currentPage - 1
    val halfDisplayed = displayedPages / 2.0f
    val start = if (realCurrentPage > halfDisplayed) (realCurrentPage - halfDisplayed) min (totalPage - displayedPages) max 0 else 0
    val end = if (realCurrentPage > halfDisplayed) (realCurrentPage + halfDisplayed) min totalPage else displayedPages min totalPage
    (Math.ceil(start).toInt, Math.ceil(end).toInt)
  }

  def navPrefix = "nav"

  def paginate(xhtml: NodeSeq) = {
    bind("nav", xhtml,
      "first" -> pageXml(1, firstXml),
      "prev" -> pageXml(currentPageNo - 1 max 1, prevXml),
      //"allpages" -> { (n: NodeSeq) => pagesXml(1 to totalPage, n) },
      "allpages" -> gt(totalPage, currentPageNo),
      "next" -> pageXml(currentPageNo + 1 min totalPage, nextXml),
      "last" -> pageXml(totalPage, lastXml))
  }

  def gt(totalPage: Int, currentPage: Long, edges: Int = 2): NodeSeq = {
    val (start, end) = interval(totalPage, currentPage)
    println(start + "|" + end)
    var pt = List[NodeSeq]()
    if (start > 0 && edges > 0) {
      val uEnd = edges min start
      pt ++= (for (i <- 0 until uEnd) yield {
        pageXml(i + 1, Text((i + 1).toString))
      })

      pt ++= (if (edges < start && (start - edges != 1)) {
        <span class="ellipse">...</span>
      } else if (start - edges == 1) {
        pageXml(edges + 2, Text((edges + 2).toString))
      } else Text(""))
    }

    pt ++= (for (i <- start until end) yield {
      pageXml(i + 1, Text((i + 1).toString))
    })

    if (end < totalPage && edges > 0) {
      pt ++= (if (totalPage - edges > end && (totalPage - edges - end != 1)) {
        <span class="ellipse">...</span>
      } else if (totalPage - edges - end == 1) {
        pageXml(end + 2, Text((end + 2).toString))
      } else Text(""))
      val sBegin = (totalPage - edges) max end
      pt ++= (for (i <- sBegin until totalPage) yield {
        pageXml(i + 1, Text((i + 1).toString))
      })
    }
    pt.flatten
  }
}

trait Paginator[T <: LongKeyedMapper[T]] extends LongKeyedMetaMapper[T] {
  self: T with LongKeyedMapper[T] =>

  def paginator(page: Long, itemsPerPage: Int, by: QueryParam[T]*): PaginatorModel[T] = {
    PaginatorModel(count(by: _*), findAll(StartAt[T](((page - 1) max 0) * itemsPerPage) :: MaxRows[T](itemsPerPage) :: by.toList: _*), page, itemsPerPage)
  }

}

object PageTest extends App {

  def interval(totalPage: Int, currentPage: Int, displayedPages: Int = 5): (Int, Int) = {
    val realCurrentPage = currentPage - 1
    val halfDisplayed = displayedPages / 2.0f
    val start = if (realCurrentPage > halfDisplayed) (realCurrentPage - halfDisplayed) min (totalPage - displayedPages) max 0 else 0
    val end = if (realCurrentPage > halfDisplayed) (realCurrentPage + halfDisplayed) min totalPage else displayedPages min totalPage
    (Math.ceil(start).toInt, Math.ceil(end).toInt)
  }

  def gt {
    val edges = 2
    val totalPage = 100
    val (start, end) = interval(totalPage, 10)
    println(start + "," + end)

    if (start > 0 && edges > 0) {
      var ends = edges min start
      for (i <- 0 until ends) {
        print(i + " ")
      }

      if (edges < start && (start - edges != 1)) {
        print("... ")
      } else if (start - edges == 1) {
        print(edges + 1 + " ")
      }
    }

    print("|")

    for (i <- start until end) {
      print(i + " ")
    }

    print("|")

    if (end < totalPage && edges > 0) {
      if (totalPage - edges > end && (totalPage - edges - end != 1)) {
        print("... ")
      } else if (totalPage - edges - end == 1) {
        print(end + 1 + " ")
      }
      var begins = (totalPage - edges) max end
      for (i <- begins until totalPage) {
        print(i + " ")
      }
    }

  }

  gt

}