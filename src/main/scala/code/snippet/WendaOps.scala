package code.snippet

import scala.xml.Text
import code.model.Wenda
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmd
import net.liftweb.util.Helpers._
import scala.xml.NodeSeq
import code.model.User
import code.model.WendaType
import code.lib.WebCacheHelper
import net.liftweb.common._
import scala.xml.XML
import net.liftweb.mapper._
import scala.collection.mutable.ArrayBuffer
import code.model.Brand
import net.liftweb.util.CssSel
import scala.xml.Unparsed

object WendaOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "create" => create
    case "list" => list
    case "view" => view
  }

  private def bies: List[QueryParam[Wenda]] = {
    val pageType = S.param("pageType").openOr("all")
    val (wendaType, keyword) = (S.param("type"), S.param("keyword"))
    val byBuffer = ArrayBuffer[QueryParam[Wenda]]()
    pageType match {
      case "common" =>
        byBuffer += By(Wenda.isRecommend, true)
        byBuffer += OrderBy(Wenda.readCount, Descending)
      case "hot" => byBuffer += OrderBy(Wenda.readCount, Descending)
      case "wait" => byBuffer += By(Wenda.replyCount, 0)
      case _ => (OrderBy(Wenda.id, Descending))
    }
    keyword match {
      case Full(k) if (!k.trim().isEmpty()) =>
        val kv = k.trim()
      case _ =>
    }
    byBuffer.toList
  }

  def list = {
    val pageType = S.param("pageType").openOr("all")
    val wendaNav =
      <ul class="nav nav-tabs">
        <li class={ if (pageType == "all") "active" else null }><a href="/wenda/all">所有问答</a></li>
        <li class={ if (pageType == "common") "active" else null }><a href="/wenda/common">常见问答</a></li>
        <li class={ if (pageType == "hot") "active" else null }><a href="/wenda/hot">热门问答</a></li>
        <li class={ if (pageType == "wait") "active" else null }><a href="/wenda/wait">待回答问题</a></li>
      </ul>

    val limit = S.attr("limit").map(_.toInt).openOr(30)
    val paginatorModel = Wenda.paginator(originalUri, bies: _*)(itemsOnPage = limit)
    val dataList = "#dataList li" #> paginatorModel.datas.map { wenda =>
      ".wendaType *" #> wenda.wendaTypeCode.displayType &
        ".stat *" #> wenda.readCount.display &
        "h3 *" #> wenda.title.displayTitle &
        ".date *" #> wenda.createdAt.asHtml
    }

    "#wendaNav" #> wendaNav & dataList & "#pagination" #> paginatorModel.paginate _
  }

  def view = {
    (for {
      id <- S.param("id").flatMap(asLong) ?~ "问答ID不存在或无效"
      wenda <- Wenda.find(By(Wenda.id, id)) ?~ s"ID为${id}的问答不存在。"
    } yield {
      wenda.readCount.incr(realIp)
      val wendaSelf = ".wenda-title *" #> wenda.title.is &
        ".wenda-content *" #> wenda.content.asHtml &
        ".stat *" #> wenda.readCount.display &
        ".date *" #> { "发布时间:" + wenda.createdAt.asHtml }

      val dataList = "#wendaReply li" #> wenda.replies.map { wendaReply =>
        ".wenda-reply-title *" #> { if (wendaReply.isRecommend.is) "最佳回答" else "其它回答" } &
          ".panel [class+]" #> { if (wendaReply.isRecommend.is) "panel-success" else null } &
          ".wenda-reply-content *" #> Unparsed(wendaReply.content.is)
      }

      wendaSelf & dataList
    }): CssSel
  }

  def creteWendaBtn(wenda: Wenda): NodeSeq = {
    if (User.loggedIn_?) {
      <button class="btn btn-success" type="button" data-toggle="modal" data-target="#createWendaDialog">我要提问</button>
    } else {
      <span><a class="btn btn-small btn-success" data-toggle="modal" data-target="#loginDialog">注册登录</a> 后可以提问。</span>
    }
  }

  def create = {
    val wenda = Wenda.create
    def process(): JsCmd = {
      wenda.asker(loginUser.id.is)
      wenda.save()
      S.redirectTo("/wenda/index")
    }

    val wendaTypes = WebCacheHelper.wendaTypes.values.toList
    "@title" #> text(wenda.title.get, wenda.title(_)) &
      "@wendaType" #> select(wendaTypes.map(v => (v.code.is.toString, v.name.is)), Empty, v => wenda.wendaTypeCode(v.toInt)) &
      "@askContent" #> textarea(wenda.content.is, wenda.content(_)) &
      "type=submit" #> ajaxSubmit("发布", process)
  }

}