package code.snippet.admin

import scala.collection.mutable.ArrayBuffer
import scala.xml._

import com.niusb.util.WebHelpers._

import code.model.Article
import code.model.ArticleStatus
import code.model.ArticleType
import code.model.Wenda
import code.model.WendaType
import code.snippet.SnippetHelper
import net.liftweb.common._
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.util.Helpers._

object WendaOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "list" => list
  }

  private def bies: List[QueryParam[Wenda]] = {
    val (wendaType, keyword, status) = (S.param("type"), S.param("keyword"), S.param("status"))
    val byBuffer = ArrayBuffer[QueryParam[Wenda]]()
    keyword match {
      case Full(k) if (!k.trim().isEmpty()) =>
        val kv = k.trim()
        byBuffer += Like(Wenda.title, s"%${kv}%")
      case _ =>
    }

    wendaType match {
      case Full(s) if (s != "all") =>
        byBuffer += By(Wenda.wendaType, WendaType(s.toInt))
      case _ =>
    }
    byBuffer.toList
  }

  def list = {
    def actions(wenda: Wenda): NodeSeq = {
      a(() => {
        BoxConfirm("确定删除【" + wenda.title.get + "】？此操作不可恢复，请谨慎！", {
          ajaxInvoke(() => { wenda.delete_!; JsCmds.Reload })._2
        })
      }, <i class="icon-trash"></i>, "class" -> "btn btn-danger")
    }

    val (wendaType, keyword, status) = (S.param("type"), S.param("keyword"), S.param("status"))
    var url = originalUri
    var wendaTypeVal, keywordVal, statusVal = ""
    wendaType match {
      case Full(t) =>
        wendaTypeVal = t
        url = appendParams(url, List("type" -> t))
      case _ =>
    }
    keyword match {
      case Full(k) if (!k.trim().isEmpty()) =>
        keywordVal = k
        url = appendParams(url, List("keyword" -> k))
      case _ =>
    }
    status match {
      case Full(s) =>
        statusVal = s
        url = appendParams(url, List("status" -> s))
      case _ =>
    }

    val paginatorModel = Wenda.paginator(url, bies: _*)()

    val searchForm = "#searchForm" #>
      <form class="form-inline" action={ url } method="get">
        <input type="text" id="keyword" name="keyword" class="span8" value={ keywordVal } placeholder="搜索标题关键词"/>
        <select id="type" name="type">
          { for ((k, v) <- Wenda.validWendaTypeSelectValues) yield <option value={ k } selected={ if (wendaTypeVal == k) "selected" else null }>{ v }</option> }
        </select>
        <button type="submit" class="btn"><i class="icon-search"></i> 搜索</button>
      </form>

    val dataList = "#dataList tr" #> paginatorModel.datas.map(wenda => {
      "#title" #> wenda.title.is &
        "#wendaType" #> wenda.wendaType &
        "#readCount" #> wenda.readCount.is &
        "#actions" #> actions(wenda)
    })
    searchForm & dataList & "#pagination" #> paginatorModel.paginate _
  }
}