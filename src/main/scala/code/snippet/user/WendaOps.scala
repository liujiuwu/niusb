package code.snippet.user

import scala.collection.mutable.ArrayBuffer
import scala.xml.NodeSeq

import com.niusb.util.WebHelpers.BoxConfirm

import code.model.Wenda
import code.model.WendaType
import code.snippet.SnippetHelper
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.SHtml.a
import net.liftweb.http.SHtml.ajaxInvoke
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds.jsExpToJsCmd
import net.liftweb.mapper.By
import net.liftweb.mapper.Like
import net.liftweb.mapper.QueryParam
import net.liftweb.util.Helpers.appendParams
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.util.Helpers.strToSuperArrowAssoc

object WendaOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "list" => list
  }

  private def bies: List[QueryParam[Wenda]] = {
    val (wendaType, keyword) = (S.param("type"), S.param("keyword"))
    val byBuffer = ArrayBuffer[QueryParam[Wenda]](By(Wenda.asker, loginUser.id.is))
    keyword match {
      case Full(k) if (!k.trim().isEmpty()) =>
        val kv = k.trim()
        byBuffer += Like(Wenda.title, s"%${kv}%")
      case _ =>
    }

    wendaType match {
      case Full(s) if (s != "all") =>
        byBuffer += By(Wenda.wendaTypeCode, s.toInt)
      case _ =>
    }
    byBuffer.toList
  }

  def list = {
    val (wendaType, keyword) = (S.param("type"), S.param("keyword"))
    var url = originalUri
    var wendaTypeVal, keywordVal = ""
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

    val paginatorModel = Wenda.paginator(url, bies: _*)()
    val searchForm = "#searchForm" #>
      <form class="form-inline" action={ url } method="get">
        <div class="form-group">
          <select class="form-control" id="type" name="type" style="width:180px">
            { WendaType.wendaTypeOptions(wendaTypeVal) }
          </select>
        </div>
        <div class="form-group">
          <input type="text" class="form-control" id="keyword" name="keyword" value={ keywordVal } placeholder="搜索标题关键词" style="width:300px"/>
        </div>
        <button type="submit" class="btn btn-primary"><i class="icon-search"></i> 搜索</button>
      </form>

    val dataList = "#dataList tr" #> paginatorModel.datas.map(wenda => {
      "#title" #> wenda.title.displayTitle() &
        "#wendaType" #> wenda.wendaTypeCode.displayType &
        "#readCount" #> wenda.readCount.is &
        "#replyCount" #> wenda.replyCount.is
    })
    searchForm & dataList & "#pagination" #> paginatorModel.paginate _
  }
}