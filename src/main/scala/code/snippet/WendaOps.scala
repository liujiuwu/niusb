package code.snippet

import scala.xml.Text
import code.model.Wenda
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmd._
import net.liftweb.http.js.JsCmds._
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
import net.liftweb.util._
import scala.xml.Unparsed
import net.liftweb.http.js.JE.JsRaw
import scala.collection.mutable.LinkedHashMap
import scala.util.Try
import scala.util.Success
import com.niusb.util.WebHelpers

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
      case code if (Try(code.toInt) match {
        case Success(c) => true
        case _ => false
      }) => byBuffer += By(Wenda.wendaTypeCode, code.toInt)
      case _ => (OrderBy(Wenda.id, Descending))
    }
    keyword match {
      case Full(k) if (!k.trim().isEmpty()) =>
        val kv = k.trim()
      case _ =>
    }
    byBuffer.toList
  }

  val wendaMenus = LinkedHashMap[String, NodeSeq](
    "/wenda/all" -> Text("所有问答"),
    "/wenda/common" -> Text("常见问答"),
    "/wenda/hot" -> Text("热门问答"),
    "/wenda/wait" -> Text("待回答问题"))

  def list = {
    val pageType = S.param("pageType").openOr("all")

    val defaultTab = for (menu <- wendaMenus) yield {
      val cls = if (menu._1.endsWith(pageType)) "active" else null
      <li class={ cls }><a href={ menu._1 }>{ menu._2 }</a></li>
    }

    val newTab = Try(pageType.toInt) match {
      case Success(wendaTypeCode) =>
        WebCacheHelper.wendaTypes.get(wendaTypeCode) match {
          case Some(wendaType) =>
            <li class="active"><a href={ "/wenda/" + wendaType.code.is }>{ wendaType.name.is }</a></li>
          case _ => Text("")
        }
      case _ => Text("")
    }

    val wendaNav =
      <ul class="nav nav-tabs">
        {
          newTab ++ defaultTab
        }
        <div class="pull-right" data-lift="WendaOps.createWendaBtn">
          <button class="btn btn-danger">我要提问</button>
        </div>
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

  /*def creteWendaBtn(wenda: Wenda): NodeSeq = {
    if (User.loggedIn_?) {
      <button class="btn btn-success" type="button" data-toggle="modal" data-target="#createWendaDialog">我要提问</button>
    } else {
      <span><a class="btn btn-small btn-success" data-toggle="modal" data-target="#loginDialog">注册登录</a> 后可以提问。</span>
    }
  }*/

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
    val wendaTypeCode = S.param("pageType") match {
      case Full(pageType) => Try(pageType.toInt) match {
        case Success(t) => t
        case _ => -1
      }
      case _ => -1
    }

    ".list-group-item" #> WebCacheHelper.wendaTypes.values.map(wendaType => {
      val active = if (wendaTypeCode >= 0 && wendaType.code.is == wendaTypeCode) "active" else null
      "a .wenda-type-name" #> wendaType.name.is &
        "a .badge" #> <span class="badge">{ wendaType.wendaCount }</span> &
        "a [href]" #> { "/wenda/" + wendaType.code.is } &
        "a [class+]" #> active
    })

  }

}