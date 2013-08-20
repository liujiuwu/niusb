package code.snippet

import code.lib.WebHelper._
import code.model.User
import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.http.RequestVar
import net.liftweb.http.S
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmd._
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper.By
import net.liftweb.util.Helpers._
import scala.xml.Text
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import java.util.Date
import code.lib.SmsHelper
import net.liftweb.http.js.JsCmds

object LoginOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "login" => login
  }

  def login = {
    var mobileBox: Box[String] = Full("")
    var pwdBox: Box[String] = Full("")

    def process(): JsCmd = {
      realMobile(mobileBox) match {
        case Full(mobile) =>
          User.find(By(User.mobile, mobile)) match {
            case Full(user) =>
              User.logUserIn(user)
              if (user.superUser.get) S.redirectTo("/admin/brand/") else S.redirectTo("/")
            case _ =>
              val user = new User
              user.mobile(mobile)
              if (user.save()) {
                User.logUserIn(user)
                S.redirectTo("/")
              } else {
                JsRaw(errorMsg("opt_login_tip", Text("注册失败，请稍候重试！")))
              }
          }
        case _ => formError("mobile", "错误的手机号！")
      }
    }

    def sendCodeSms(mobile: String): JsCmd = {
      SmsHelper.sendCodeSms(mobile)
      Alert("已发送")
    }

    "@mobile" #> ajaxText(mobileBox.get, mobile => {
      realMobile(Full(mobile)) match {
        case Full(m) =>
          mobileBox = Full(m)
          removeFormError("mobile") & JsRaw("""$("#login_btn").removeClass("disabled")""") & JsRaw("""$("#getCodeBtn").removeClass("disabled")""")
        case _ => JsRaw("""$("#getCodeBtn").addClass("disabled")""") & JsRaw("""$("#login_btn").addClass("disabled")""") & formError("mobile", "错误的手机号！")
      }
    }) &
      "@pwd" #> password(pwdBox.get, pwd => pwdBox = Full(pwd)) &
      "@getCodeBtn [onclick]" #> ajaxCall("$('#mobile').val()", mobile => sendCodeSms(mobile)) &
      "@sub" #> hidden(process)
  }
}