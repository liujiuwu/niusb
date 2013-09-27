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
import code.model.User
import code.model.Wenda
import code.model.WendaReply
import code.model.WendaType
import net.liftweb.common.Box
import net.liftweb.common.Box.box2Option
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.RequestVar
import net.liftweb.http.S
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.SHtml.a
import net.liftweb.http.SHtml.ajaxSubmit
import net.liftweb.http.SHtml.select
import net.liftweb.http.SHtml.text
import net.liftweb.http.SHtml.textarea
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds.Reload
import net.liftweb.http.js.JsCmds.jsExpToJsCmd
import net.liftweb.mapper.By
import net.liftweb.mapper.{ By_< => By_< }
import net.liftweb.mapper.{ By_> => By_> }
import net.liftweb.mapper.Descending
import net.liftweb.mapper.OrderBy
import net.liftweb.mapper.QueryParam
import net.liftweb.util.CssSel
import net.liftweb.util.Helpers.asLong
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import net.liftweb.util.ClearNodes

object WendaOps extends DispatchSnippet with SnippetHelper with Loggable {
  private object wendaIdVar extends RequestVar[Box[Long]](Empty)

  def dispatch = {
    case "create" => create
    case "reply" => reply
    case "list" => list
    case "view" => view
    case "wendaTypes" => wendaTypes
    case "createWendaBtn" => createWendaBtn
  }

  val wendaMenus = LinkedHashMap[String, NodeSeq](
    "/wenda/0/-1/0" -> Text("全部问答"),
    "/wenda/1/-1/0" -> Text("常见问答"),
    "/wenda/2/-1/0" -> Text("待回答问题"),
    "/wenda/3/-1/0" -> Text("热门问答"))

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
        case 3 =>
          byBuffer += By_>(Wenda.readCount, 10)
          byBuffer += OrderBy(Wenda.readCount, Descending)
      }
    }

    if (wendaTypeCode >= 0) {
      byBuffer += By(Wenda.wendaTypeCode, wendaTypeCode)
    }

    orderType match {
      case 0 => byBuffer += OrderBy(Wenda.id, Descending)
      case 1 => byBuffer += OrderBy(Wenda.readCount, Descending)
      case _ => byBuffer += OrderBy(Wenda.id, Descending)
    }

    val defaultTab = for (menu <- wendaMenus) yield {
      val cls = if (menu._1.startsWith("/wenda/" + pageType)) "active" else null
      <li class={ cls }><a href={ menu._1 }>{ menu._2 }</a></li>
    }

    val wendaNav = <ul class="nav nav-tabs">
                     { defaultTab }
                     <div class="pull-right" data-lift="WendaOps.createWendaBtn">
                       <button id="createWendaBtn" class="btn btn-danger">我要提问</button>
                     </div>
                   </ul>

    val pageUrl = Wenda.pageUrl(pageType, wendaTypeCode, orderType)
    val paginatorModel = Wenda.paginator(pageUrl, byBuffer.toList: _*)(itemsOnPage = limit)
    val dataList = "#dataList li" #> paginatorModel.datas.map { wenda =>
      ".wendaType *" #> wenda.wendaTypeCode.displayType &
        ".stat *" #> { wenda.readCount.display ++ Text("/ ") ++ wenda.replyCount.display } &
        "h3 *" #> wenda.title.displayTitle &
        ".date *" #> wenda.createdAt.asHtml
    }

    "#wendaNav" #> wendaNav & dataList & "#pagination" #> paginatorModel.paginate _
  }

  def view = {
    def reply(): JsCmd = {
      User.currentUser match {
        case Full(user) =>
          JsRaw("""$("#replyWendaDialog").modal();editorInit()""")
        case _ => WebHelpers.showLoginModal("login-panel")
      }
    }

    def acceptTreply(wenda: Wenda, wendaReply: WendaReply): JsCmd = {
      wenda.replyCount.incr
      wendaReply.isRecommend(true)
      wendaReply.save()
      Reload
    }

    (for {
      id <- S.param("id").flatMap(asLong) ?~ "问答ID不存在或无效"
      wenda <- Wenda.find(By(Wenda.id, id)) ?~ s"ID为${id}的问答不存在。"
    } yield {
      wendaIdVar(Full(wenda.id.is))
      val asker = User.find(By(User.id, wenda.asker.is)) match {
        case Full(u) => u.displayMaskName
        case _ => s"""<a href="${WebHelpers.WebSiteUrlAndName._1}" target="_blank">${WebHelpers.WebSiteUrlAndName._2}</a>"""
      }
      wenda.readCount.incr(realIp)
      val wendaSelf = ".wenda-title *" #> wenda.title.is &
        ".wenda-content *" #> Unparsed(wenda.content.is) &
        ".stat -*" #> <em>{ wenda.readCount.is }</em> &
        ".author *+" #> asker &
        ".date *+" #> wenda.createdAt.asHtml

      val replies = wenda.replies
      val isRecommend = replies.headOption match {
        case Some(r) => r.isRecommend.is
        case _ => false
      }

      val replyWendaBtn = "#replyWendaBtn" #> (if (isRecommend) Text("") else <span>{ a(() => reply, Text("我来回答"), "class" -> ("btn  btn-xs " + WebHelpers.btnCss)) }</span>)
      val dataList = "#wendaReply li" #> replies.map { wendaReply =>
        val replyer = User.find(By(User.id, wendaReply.reply.is)) match {
          case Full(u) => u.displayMaskName
          case _ => s"""<a href="${WebHelpers.WebSiteUrlAndName._1}" target="_blank">${WebHelpers.WebSiteUrlAndName._2}</a>"""
        }

        val acceptReplyWendaBtn = "#acceptReplyWendaBtn" #> (if (isRecommend) Text("") else <span>{ a(() => acceptTreply(wenda, wendaReply), Text("采纳为最佳答案"), "class" -> ("btn btn-xs " + WebHelpers.btnCss)) }</span>)

        ".wenda-reply-title *" #> { if (wendaReply.isRecommend.is) "最佳回答" else "回答编号#" + wendaReply.id.is } &
          ".panel [class+]" #> { if (wendaReply.isRecommend.is) "panel-success" else null } &
          ".wenda-reply-content *" #> Unparsed(wendaReply.content.is) &
          ".reply-author *+" #> replyer &
          ".reply-date *+" #> wendaReply.createdAt.asHtml & acceptReplyWendaBtn
      }

      wendaSelf & dataList & replyWendaBtn
    }): CssSel
  }

  def create = {
    var title, askContent = ""
    val wenda = Wenda.create
    def process(): JsCmd = {
      User.currentUser match {
        case Full(user) =>
          wenda.asker(user.id.is)
        case _ =>
          return WebHelpers.showLoginModal("login-panel")
      }

      if (title.trim().isEmpty()) {
        return WebHelpers.formError("title", "请填写问题的标题，8~30字之间")
      } else if (title.trim().length() < 8 || title.trim().length() > 30) {
        return WebHelpers.formError("title", "标题请确认在8~30字之间")
      } else {
        wenda.title(title)
      }

      if (askContent.trim().isEmpty()) {
        return WebHelpers.formError("askContent", "请填写问题的详细内容，8~500字之间")
      } else if (askContent.trim().length() < 8 || askContent.trim().length() > 500) {
        return WebHelpers.formError("askContent", "标题请确认在8~500字之间")
      } else {
        wenda.content(askContent)
      }

      WebCacheHelper.wendaTypes.get(wenda.wendaTypeCode.is) match {
        case Some(wendaType) => wendaType.wendaCount.incr()
        case _ =>
      }
      wenda.save()
      Reload
    }

    val wendaTypes = WebCacheHelper.wendaTypes.values.toList
    "@title" #> text(title, title = _) &
      "@wendaType" #> select(wendaTypes.map(v => (v.code.is.toString, v.name.is)), Empty, v => wenda.wendaTypeCode(v.toInt)) &
      "@askContent" #> textarea(askContent, askContent = _) &
      ":submit" #> ajaxSubmit("确认发布", process)
  }

  def reply = {
    var content = ""
    def process(): JsCmd = {
      if (content.trim().isEmpty()) {
        return WebHelpers.formError("replyContent", "请填写回复内容，8~2000字之间")
      }
      wendaIdVar.is match {
        case Full(wendaId) =>
          Wenda.find(By(Wenda.id, wendaId)) match {
            case Full(wenda) =>
              val wendaReply = WendaReply.create
              wendaReply.wenda(wendaId)
              wendaReply.content(content)
              wendaReply.reply(User.currentUser.get.id.is)
              wendaReply.save
            case _ =>
          }
        case _ =>
      }
      Reload
    }

    "@replyContent" #> textarea(content, content = _) &
      "type=submit" #> ajaxSubmit("确认回答", process)
  }

  def createWendaBtn = {
    def process(): JsCmd = {
      User.currentUser match {
        case Full(user) =>
          JsRaw("""$("#createWendaDialog").modal();editorInit()""")
        case _ => WebHelpers.showLoginModal("login-panel")
      }
    }
    "#createWendaBtn " #> a(() => process, Text("我要提问?"), "class" -> ("btn " + WebHelpers.btnCss))
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
        "a [href]" #> { Wenda.pageUrl(pageType, wendaType.code.is, orderType) } &
        "a [class+]" #> active
    })
  }
}