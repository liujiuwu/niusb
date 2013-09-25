package code.snippet

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.LinkedHashMap
import scala.util.Success
import scala.util.Try
import scala.xml.NodeSeq
import scala.xml.Text
import scala.xml.Unparsed

import com.niusb.util.WebHelpers

import code.lib.WebCacheHelper
import code.model.Brand
import code.model.User
import code.model.Wenda
import code.model.WendaType
import net.liftweb.common._
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmd._
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.util.Helpers._

object WendaOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "create" => create
    case "reply" => reply
    case "list" => list
    case "view" => view
    case "wendaTypes" => wendaTypes
    case "createWendaBtn" => createWendaBtn
    case "replyWendaBtn" => replyWendaBtn
  }

  val wendaMenus = LinkedHashMap[String, NodeSeq](
    "/wenda/0/-1/0" -> Text("全部问答"),
    "/wenda/1/-1/0" -> Text("常见问答"),
    "/wenda/2/-1/0" -> Text("待回答问题"))

  def list = {
    val limit = S.attr("limit").map(_.toInt).openOr(30)
    val pageType = Try(S.param("pageType").openOr("0").toInt) match {
      case Success(code) => code
      case _ => 0
    }

    val wendaTypeCode = Try(S.param("wendaTypeCode").openOr("-1").toInt) match {
      case Success(code) => code
      case _ => -1
    }

    val wendaTypeName = WebCacheHelper.wendaTypes.get(wendaTypeCode) match {
      case Some(wendaType) => wendaType.name.is
      case _ => "全部问答"
    }

    val orderType = Try(S.param("orderType").openOr("0").toInt) match {
      case Success(code) => code
      case _ => 0
    }

    val byBuffer = ArrayBuffer[QueryParam[Wenda]]()
    if (pageType > 0) {
      pageType match {
        case 1 => byBuffer += By(Wenda.isRecommend, true)
        case 2 => byBuffer += By_<(Wenda.replyCount, 0)
      }
    }

    if (wendaTypeCode >= 0) {
      byBuffer += By(Wenda.wendaTypeCode, wendaTypeCode)
    }

    orderType match {
      case 0 => byBuffer += OrderBy(Wenda.id, Descending)
      case 1 => byBuffer += OrderBy(Wenda.readCount, Ascending)
      case _ => byBuffer += OrderBy(Wenda.id, Descending)
    }

    val defaultTab = for (menu <- wendaMenus) yield {
      val cls = if (menu._1.startsWith("/wenda/" + pageType)) "active" else null
      <li class={ cls }><a href={ menu._1 }>{ menu._2 }</a></li>
    }

    val wendaNav = <ul class="nav nav-tabs">
                     { defaultTab }
                     <div class="pull-right" data-lift="WendaOps.createWendaBtn">
                       <button class="btn btn-danger">我要提问</button>
                     </div>
                   </ul>

    val pageUrl = "/wenda/" + pageType + "/" + wendaTypeCode + "/" + orderType
    val paginatorModel = Wenda.paginator(pageUrl, byBuffer.toList: _*)(itemsOnPage = limit)
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
        ".wenda-content *" #> Unparsed(wenda.content.is) &
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
      "type=submit" #> ajaxSubmit("确认发布", process)
  }

  def reply = {
    def process(): JsCmd = {
      S.redirectTo("/wenda/index")
    }

    "@replyContent" #> textarea("", println(_)) &
      "type=submit" #> ajaxSubmit("确认回答", process)
  }

  private def btnCss = {
    val isLogined = User.currentUser match {
      case Full(user) => true
      case _ => false
    }
    if (isLogined) "btn-success" else "btn-danger"
  }

  def createWendaBtn = {
    def process(): JsCmd = {
      User.currentUser match {
        case Full(user) =>
          JsRaw("""$("#createWendaDialog").modal();editorInit()""")
        case _ => WebHelpers.showLoginModal("login-panel")
      }
    }
    ".btn" #> a(() => process, Text("我要提问?"), "class" -> ("btn " + btnCss))
  }

  def replyWendaBtn = {
    def process(): JsCmd = {
      User.currentUser match {
        case Full(user) =>
          JsRaw("""$("#replyWendaDialog").modal();editorInit()""")
        case _ => WebHelpers.showLoginModal("login-panel")
      }
    }
    ".btn" #> a(() => process, Text("我来回答"), "class" -> ("btn " + btnCss))
  }

  def wendaTypes = {
    val pageType = Try(S.param("pageType").openOr("0").toInt) match {
      case Success(code) => code
      case _ => 0
    }

    val wendaTypeCode = Try(S.param("wendaTypeCode").openOr("-1").toInt) match {
      case Success(code) => code
      case _ => -1
    }

    val orderType = Try(S.param("orderType").openOr("0").toInt) match {
      case Success(code) => code
      case _ => 0
    }

    val allWendaType = WendaType.create
    allWendaType.code(-1)
    allWendaType.name("所有分类")
    allWendaType.wendaCount(WebCacheHelper.wendaTypes.values.toList.foldLeft(0)(_ + _.wendaCount.is))

    val allWendaTypes = allWendaType :: WebCacheHelper.wendaTypes.values.toList
    ".list-group-item" #> allWendaTypes.map(wendaType => {
      val active = if (wendaType.code.is == wendaTypeCode) "active" else null
      "a .wenda-type-name" #> wendaType.name.is &
        "a .badge" #> <span class="badge">{ wendaType.wendaCount }</span> &
        "a [href]" #> { "/wenda/" + pageType + "/" + wendaType.code.is + "/" + orderType } &
        "a [class+]" #> active
    })
  }
}