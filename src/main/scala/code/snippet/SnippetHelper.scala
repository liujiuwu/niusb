package code.snippet

import scala.xml.NodeSeq
import code.model.User
import net.liftweb.common.Box
import net.liftweb.common.Box.box2Option
import net.liftweb.common.Empty
import net.liftweb.common.Failure
import net.liftweb.common.Full
import net.liftweb.http.RequestVar
import net.liftweb.http.S
import net.liftweb.util.CssSel
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._

object SnippetHelper extends SnippetHelper

trait SnippetHelper {
  object tabMenuRV extends RequestVar[Box[(String, String)]](Empty)
  protected def loginUser = User.currentUser.openOrThrowException("not found user")

  def realIp = {
    val ip = S.containerRequest.map(_.remoteAddress).openOr("")
    if (ip == "") {
      S.request.flatMap(_.header("X-Real-IP")).openOr("127.0.0.1")
    } else {
      ip
    }
  }

  def originalUri = S.originalRequest.map(_.uri).openOr(sys.error("No request"))

  def redirectUrl(default: String = originalUri) = S.originalRequest.get.request.queryString match {
    case Full(queryString) => default + "?" + queryString
    case _ => default
  }

  def alertHtml(msg: NodeSeq, title: String = "出错啦！", alertType: String = "danger"): NodeSeq = {
    <div class={ "alert alert-" + alertType }>
      <button type="button" class="close" data-dismiss="alert">&times;</button>
      <h4>{ title }</h4>
      { msg }
    </div>
  }

  def noticeHtml(msg: NodeSeq, title: String = "消息提示！"): NodeSeq = alertHtml(msg, title, "info")
  def warningHtml(msg: NodeSeq, title: String = "警告！"): NodeSeq = alertHtml(msg, title, "warning")
  def errorHtml(msg: NodeSeq, title: String = "出错啦！"): NodeSeq = alertHtml(msg, title, "danger")

  def requiredLogin(tip: String, logined: => NodeSeq): NodeSeq = {
    if (User.loggedIn_?) {
      logined
    } else {
      <span><a class="btn btn-small btn-success" data-toggle="modal" data-target="#loginDialog">注册登录</a> 后可以{ tip }。</span>
    }
  }

  def requiredLogin(execJs: => JsCmd): JsCmd = {
    if (User.loggedIn_?) execJs else JsRaw("""$("#loginDialog").modal();""")
  }

  implicit protected def boxCssSelToCssSel(in: Box[CssSel]): CssSel = in match {
    case Full(cssSel) => cssSel
    case Failure(msg, except, _) =>
      var resultMsg = msg
      except.foreach {
        case e: NoSuchElementException =>
          resultMsg = "数据不存在，请检查！"
        case e: NumberFormatException =>
          resultMsg = "数字格式错误，请检查！"
        case e: Exception =>
          resultMsg = "操作发生异常，请稍候重试或联系我们！"
      }
      "*" #> errorHtml(<p>{ resultMsg }</p>)
    case _ => "*" #> errorHtml(<p>404</p>)
  }
}