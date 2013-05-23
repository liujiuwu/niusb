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
        <a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="icon-user"></i> { if (user.name.get.isEmpty()) user.mobile.get else user.name.get } <b class='caret'></b></a>
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

  def login = {
    val mobileRegx = """^(13[0-9]|15[0|3|6|7|8|9]|18[8|9])(\d{8})$""".r
    def process(): JsCmd = {
      //JsRaw("$('#loginDialog').modal('hide')")
      mobileVar.get match {
        case Full(mobile) => mobile match {
          case mobileRegx(mp, ms) =>
            User.find(By(User.mobile, mp + ms)) match {
              case Full(user) =>
                User.logUserIn(user)
                S.redirectTo("/")
              case _ => {
                val user = new User
                user.mobile(mobile)
                user.password("123456")
                user.validate match {
                  case Nil =>
                    if (user.save) {
                      User.logUserIn(user)
                      S.redirectTo("/")
                    } else {
                      Alert("注册失败，请稍候重试！")
                    }
                  case errors => println("============----" + errors)
                }
              }
            }
          case _ => Alert("手机号码错误")
        }
        case _ => Alert("手机号码错误")
      }
    }

    "@mobile" #> ajaxText(mobileVar.get.openOr(""), mobile => {
      mobileVar(Box.legacyNullTest(mobile))
      mobile match {
        case mobileRegx(mp, ms) =>
        case _ => Alert("不合法的手机号")
      }
    }) &
      "@pwd" #> password(pwdVar.get.openOr(""), pwd => pwdVar(Box.legacyNullTest(pwd))) &
      "@sub" #> hidden(process)
  }

}