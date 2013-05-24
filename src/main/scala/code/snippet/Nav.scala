package code.snippet

import scala.xml.NodeSeq
import code.model.User
import net.liftweb.common.Full
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http.RequestVar
import net.liftweb.http.SHtml
import net.liftweb.http._
import net.liftweb.http.SHtml._
import net.liftweb.mapper.By
import net.liftweb.http.S
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import scala.xml.Text
import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.http.js.JE.JsRaw
import code.lib.WebHelper

object Nav {
  private object mobileVar extends RequestVar[Box[String]](Empty)
  private object pwdVar extends RequestVar[Box[String]](Empty)

  def currentActiveLink = {
    val userUrl = """(^/user.*)""".r
    ("li class=" + (S.uri match {
      case "/" => "home"
      case "/market/" => "market"
      case "/wenda/" => "wenda"
      case userUrl(url) => "user_index"
      case url => url.replaceAll("^/|/$", "").replaceAll("/", "_")
    }) + " [class+]") #> "active"
  }

  def userStatus = User.currentUser match {
    case Full(user) =>
      <li class="dropdown">
        <a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="icon-user"></i> <span id="topBarUserName">{ if (user.name.get.isEmpty()) user.mobile.get else user.name.get } </span><b class='caret'></b></a>
        <ul class="dropdown-menu">
          <lift:Menu.group group="userMenu">
            <li><menu:bind/></li>
          </lift:Menu.group>
          <li class="divider"></li>
          <li class="last"><a href="/user/sign_out"><span><i class="icon-signout"></i> 退出</span></a></li>
        </ul>
      </li>
    case _ =>
      <button class="btn btn-small btn-success" type="button" data-toggle="modal" data-target="#loginDialog">会员登录或注册</button>
  }

  def getRealMobile(mobile: Box[String]): Box[String] = {
    val mobileRegx = """^(13[0-9]|15[0|3|6|7|8|9]|18[8|9])(\d{8})$""".r
    mobile.openOr(Empty) match {
      case mobileRegx(mp, ms) => Full(mp + ms)
      case _ => Empty
    }
  }

  def login = {
    def process(): JsCmd = {
      getRealMobile(mobileVar.get) match {
        case Full(mobile) =>
          println(mobile+"-------------")
          User.find(By(User.mobile, mobile)) match {
            case Full(user) =>
              User.logUserIn(user)
              S.redirectTo("/")
            case _ =>
              val user = new User
              user.mobile(mobile)
              if (user.save) {
                User.logUserIn(user)
                S.redirectTo("/")
              } else {
                JsRaw(WebHelper.errorMsg("opt_login_tip", Text("注册失败，请稍候重试！")))
              }
          }
        case _ => WebHelper.formError("mobile", "错误的手机号！")
      }
    }

    "@mobile" #> ajaxText(mobileVar.get.openOr(""), mobile => {
      getRealMobile(Box.legacyNullTest(mobile)) match {
        case Full(m) =>
          mobileVar(Full(m))
          WebHelper.removeFormError("mobile") & JsRaw("""$("#login_btn").removeClass("disabled")""") & JsRaw("""$("#getCode_btn").removeClass("disabled")""")
        case _ => JsRaw("""$("#getCode_btn").addClass("disabled")""") & JsRaw("""$("#login_btn").addClass("disabled")""") & WebHelper.formError("mobile", "错误的手机号！")
      }
    }) &
      "@pwd" #> password(pwdVar.get.openOr(""), pwd => pwdVar(Box.legacyNullTest(pwd))) &
      "@sub" #> hidden(process)
  }

}