package code.snippet

import scala.xml.NodeSeq
import code.model.User
import net.liftweb.common.Full
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http.RequestVar
import net.liftweb.http.SHtml
import net.liftweb.mapper.By
import net.liftweb.http.S

object Nav {
  private object mobileVar extends RequestVar[String]("13826526941")
  private object passwordVar extends RequestVar[String]("123456")

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
        <a href="#" class="user_index dropdown-toggle" data-toggle="dropdown"><i class="icon-user"></i> { if (user.name.is.isEmpty()) user.mobile else user.name } <b class='caret'></b></a>
        <ul class="dropdown-menu">
          <li><a href="/user/profile"><span><i class="icon-edit"></i> 帐户信息</span></a></li>
          <li><a href="/user/brand/add"><span><i class="icon-plus"></i> 发布商标</span></a></li>
          <li><a href="/user/brand/"><span><i class="icon-list"></i> 我的商标</span></a></li>
          <li class="divider"></li>
          <li class="last"><a href="/user/sign_out"><span><i class="icon-signout"></i> 退出</span></a></li>
        </ul>
      </li>
    case _ =>
      <button class="btn btn-small btn-success" type="button" data-toggle="modal" data-target="#login">会员登录或注册</button>
  }

  def login = {
    def processForm() = {
      println(mobileVar.is + "====================")
      User.find(By(User.mobile, mobileVar.is)) match {
        case Full(user) => {
          if (user.password.match_?(passwordVar.is)) {
            User.logUserIn(user, () => S.redirectTo("/"))
          }
        }
        case _ =>
      }
    }

    "name=mobile" #> SHtml.text(mobileVar.is, mobileVar(_)) &
      "name=password" #> SHtml.password(passwordVar.is, passwordVar(_)) &
      "type=submit" #> SHtml.onSubmitUnit(processForm)
  }

}