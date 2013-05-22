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
        <a href="#" class="user_index dropdown-toggle" data-toggle="dropdown"><i class="icon-user"></i> { if (user.name.get.isEmpty()) user.mobile.get else user.name.get } <b class='caret'></b></a>
        <ul class="dropdown-menu">
          <li><a href="/user/profile"><span><i class="icon-edit"></i> 帐户信息</span></a></li>
          <li><a href="/user/brand/add"><span><i class="icon-plus"></i> 发布商标</span></a></li>
          <li><a href="/user/brand/"><span><i class="icon-list"></i> 我的商标</span></a></li>
          <li class="divider"></li>
          <li class="last"><a href="/user/sign_out"><span><i class="icon-signout"></i> 退出</span></a></li>
        </ul>
      </li>
    case _ =>
      <button class="btn btn-small btn-success" type="button" data-toggle="modal" data-target="#loginDialog">会员登录或注册</button>
  }

  def login = {
    def process(): JsCmd = {
      //JsRaw("$('#loginDialog').modal('hide')")
      mobileVar.is match {
        case Full(mobile) =>
        println(mobile)
          User.find(By(User.mobile, mobile)) match {
            case Full(user) =>
              User.logUserIn(user)
              S.redirectTo("/")
            case _ => {
              val user = new User
              user.mobile(mobile)
              user.password("123456")
              user.validate match {
                case Nil => user.save
                case errors => println("============----"+errors)
              }
            }
          }
        case _ => Alert("mobile is empty")
      }

      Alert("fail")
    }

    "@mobile" #> text(mobileVar.get.openOr(""), mobile => mobileVar(Full(mobile))) &
      "@pwd" #> password(pwdVar.get.openOr(""), pwd => pwdVar(Full(pwd))) &
      "@sub" #> hidden(process)
  }

}