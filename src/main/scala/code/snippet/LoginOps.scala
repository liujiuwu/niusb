package code.snippet

import scala.xml.Text
import code.lib.SmsHelper
import code.lib.WebHelper._
import code.model.User
import net.liftweb.common.Box
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JE.ValById
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmd._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsCmds
import net.liftweb.mapper.By
import net.liftweb.util.Helpers._
import code.lib.MemcachedHelper
import code.lib.SmsCode
import code.lib.WebHelper
import net.liftweb.util.StringHelpers
import net.liftweb.util.Helpers

object LoginOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "login" => login
  }

  def login = {
    var mobileBox: Box[String] = Full("")
    var pwdBox: Box[String] = Full("")

    def process(): JsCmd = {
      val mobile = realMobile(mobileBox) match {
        case Full(mb) => mb
        case _ => return formError("mobile", "请输入正确的手机号！")
      }

      val pwd = pwdBox match {
        case Full(p) if (!p.trim.isEmpty()) => p
        case _ => return removeFormError() & formError("pwd", "请输入短信验证码或密码！")
      }

      val code = SmsHelper.smsCode(mobile)._1
      User.find(By(User.mobile, mobile)) match {
        case Full(user) =>
          if (user.authSmsCodeOrPwd(pwd)) {
            user.lastLoginTime(user.loginTime.get)
            user.loginTime(Helpers.now)
            user.save()
            User.logUserIn(user)
            S.redirectTo(redirectUrl())
          } else {
            formError("pwd", "验证码或密码错误，请确认！")
          }
        case _ =>
          val user = User.create
          user.mobile(mobile)
          user.password(StringHelpers.randomString(6))
          if (user.authSmsCodeOrPwd(pwd)) {
            user.loginTime(Helpers.now)
            user.lastLoginTime(user.loginTime.get)
            user.save()
            User.logUserIn(user)
            S.redirectTo(redirectUrl())
          } else {
            formError("pwd", "验证码错误，请确认！")
          }
      }
    }

    def sendCodeSms(mobileVal: String): JsCmd = {
      val mobile = realMobile(Full(mobileVal)) match {
        case Full(mb) => mb
        case _ => return formError("mobile", "请输入正确的手机号！")
      }

      val cacheTime = SmsHelper.smsCode(mobile)._2
      removeFormError() & (if ((WebHelper.now - cacheTime) > 60) {
        SmsHelper.sendCodeSms(mobile)
        JsRaw("""$("#getCodeBtn").countdown();$("#opt_login_tip").hide().text("")""")
      } else {
        JsRaw("""$("#opt_login_tip").show().text("验证码已经发送至%s，请查看短信获取！")""".format(mobile))
      }) & Alert(SmsHelper.smsCode(mobile)._1)
    }

    "@mobile" #> text(mobileBox.get, mobile => mobileBox = Full(mobile)) &
      "@pwd" #> password(pwdBox.get, pwd => pwdBox = Full(pwd)) &
      "@getCodeBtn [onclick]" #> ajaxCall(ValById("mobile"), sendCodeSms) &
      "type=submit" #> ajaxSubmit("登录", process)
  }
}