package code.snippet

import scala.xml.Text
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
import com.niusb.util.SmsCode
import net.liftweb.util.StringHelpers
import net.liftweb.util.Helpers
import com.niusb.util.WebHelpers
import com.niusb.util.SmsHelpers

object LoginOps extends DispatchSnippet with SnippetHelper with Loggable {
  val mobileError = "请输入正确的手机号！"
  def dispatch = {
    case "login" => login
    case "create" => create
  }

  /*  def create = {
    var mobileBox: Box[String] = Full("")
    var pwdBox: Box[String] = Full("")

    def process(): JsCmd = {
      val mobile = WebHelpers.realMobile(mobileBox) match {
        case Full(mb) => mb
        case _ => return WebHelpers.formError("mobile", "请输入正确的手机号！")
      }

      val pwd = pwdBox match {
        case Full(p) if (!p.trim.isEmpty()) => p
        case _ => return WebHelpers.removeFormError() & WebHelpers.formError("pwd", "请输入短信验证码或密码！")
      }

      val code = SmsHelpers.smsCode(mobile).code
      User.find(By(User.mobile, mobile)) match {
        case Full(user) =>
          if (user.authSmsCodeOrPwd(pwd)) {
            user.lastLoginTime(user.loginTime.get)
            user.loginTime(Helpers.now)
            user.save()
            User.logUserIn(user)
            S.redirectTo(redirectUrl())
          } else {
            WebHelpers.formError("pwd", "验证码或密码错误，请确认！")
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
            WebHelpers.formError("pwd", "验证码错误，请确认！")
          }
      }
    }

    def sendCodeSms(mobileVal: String): JsCmd = {
      val mobile = WebHelpers.realMobile(Full(mobileVal)) match {
        case Full(mb) => mb
        case _ => return WebHelpers.formError("mobile", "请输入正确的手机号！")
      }

      val cacheTime = SmsHelpers.smsCode(mobile).cacheTime
      WebHelpers.removeFormError() & (if ((WebHelpers.now - cacheTime) > 60) {
        SmsHelpers.sendCodeSms(mobile)
        JsRaw("""$("#getCodeBtn").countdown();$("#opt_login_tip").hide().text("")""")
      } else {
        JsRaw("""$("#opt_login_tip").show().text("验证码已经发送至%s，请查看短信获取！")""".format(mobile))
      }) & Alert(SmsHelpers.smsCode(mobile).code)
    }

    "@mobile" #> text(mobileBox.get, mobile => mobileBox = Full(mobile)) &
      "@pwd" #> password(pwdBox.get, pwd => pwdBox = Full(pwd)) &
      "@getCodeBtn [onclick]" #> ajaxCall(ValById("mobile"), sendCodeSms) &
      "type=submit" #> ajaxSubmit("登录", process)
  }*/

  private def isRegUser(mobile: String): Boolean = {
    User.find(By(User.mobile, mobile)) match {
      case Full(user) => true
      case _ => false
    }
  }

  def login = {
    var mobile, pwd = ""

    def process(): JsCmd = {
      WebHelpers.realMobile(Full(mobile)) match {
        case Full(mb) =>
          User.find(By(User.mobile, mobile)) match {
            case Full(user) =>
              if (pwd.trim().isEmpty()) {
                return WebHelpers.formError("loginPwd", "请输入登录密码！")
              }

              if (!user.password.match_?(pwd)) {
                return WebHelpers.formError("loginPwd", "登录密码错误，请确认！")
              }
              user.lastLoginTime(user.loginTime.get)
              user.loginTime(Helpers.now)
              user.save()
              User.logUserIn(user)
              S.redirectTo(redirectUrl())
            case _ => return WebHelpers.formError("loginMobile", "此手机号还未注册，请先注册！")
          }
        case _ => return WebHelpers.formError("loginMobile", mobileError)
      }
    }

    "@loginMobile" #> text(mobile, mobile = _) &
      "@loginPwd" #> password(pwd, pwd = _) &
      "type=submit" #> ajaxSubmit("登录", process)
  }

  def create = {
    var mobile, code, pwd = ""

    def process(): JsCmd = {
      WebHelpers.realMobile(Full(mobile)) match {
        case Full(mb) =>
          if (isRegUser(mb)) {
            return WebHelpers.formError("regMobile", "此手机号已经注册，请登录！")
          }

          if (code.trim().isEmpty()) {
            return WebHelpers.formError("regCode", "请输入短信验证码或密码！")
          }

          val smsCode = SmsHelpers.smsCode(mobile).code
          if (smsCode != code) {
            return WebHelpers.formError("regCode", "短信验证码错误，请确认！")
          }

          if (pwd.trim().isEmpty()) {
            return WebHelpers.formError("regPwd", "请设置您的密码！")
          }

          val user = User.create
          user.mobile(mobile)
          user.password(pwd)
          user.loginTime(Helpers.now)
          user.lastLoginTime(user.loginTime.get)
          user.save()
          User.logUserIn(user)
          S.redirectTo(redirectUrl())
        case _ => return WebHelpers.formError("regMobile", mobileError)
      }
    }

    def sendCodeSms(mobileVal: String): JsCmd = {
      WebHelpers.realMobile(Full(mobileVal)) match {
        case Full(mb) =>
          if (isRegUser(mb)) {
            return WebHelpers.formError("regMobile", "此手机号已经注册，请登录！")
          }

          val cacheTime = SmsHelpers.smsCode(mobile).cacheTime
          WebHelpers.removeFormError() & (if ((WebHelpers.now - cacheTime) > 60) {
            SmsHelpers.sendCodeSms(mobile)
            JsRaw("""$("#getCodeBtn").countdown();$("#opt_login_tip").hide().text("")""")
          } else {
            JsRaw("""$("#opt_login_tip").show().text("验证码已经发送至%s，请查看短信获取！")""".format(mobile))
          }) & Alert(SmsHelpers.smsCode(mobile).code)
        case _ => return WebHelpers.formError("regMobile", mobileError)
      }
    }

    "@regMobile" #> text(mobile, mobile = _) &
      "@regCode" #> text(code, code = _) &
      "@regPwd" #> password(pwd, pwd = _) &
      "@getCodeBtn [onclick]" #> ajaxCall(ValById("regMobile"), sendCodeSms) &
      "type=submit" #> ajaxSubmit("确认注册", process)
  }
}