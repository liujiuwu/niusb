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
import net.liftweb.http.SHtml
import net.liftweb.util.PassThru
import code.lib.WebCacheHelper

object LoginOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "login" => login
    case "create" => create
    case "forgot" => forgot
  }

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
          User.find(By(User.mobile, mb)) match {
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
        case _ => return WebHelpers.formError("loginMobile", "请输入正确的手机号！")
      }
    }

    "@loginMobile" #> text(mobile, mobile = _) &
      "@loginPwd" #> password(pwd, pwd = _) &
      "#forgotPwdBtn [onclick]" #> ajaxInvoke(() => WebHelpers.showLoginModal("forgot-panel")) &
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

          val smsCode = SmsHelpers.smsCode(mb).code
          if (smsCode != code) {
            return WebHelpers.formError("regCode", "短信验证码错误，请确认！")
          }

          if (pwd.trim().isEmpty()) {
            return WebHelpers.formError("regPwd", "请设置您的密码！")
          }

          val user = User.create
          user.mobile(mb)
          user.password(pwd)
          user.loginTime(Helpers.now)
          user.lastLoginTime(user.loginTime.get)
          user.save()
          User.logUserIn(user)
          S.redirectTo(redirectUrl())
        case _ => return WebHelpers.formError("regMobile", "请输入正确的手机号！")
      }
    }

    def sendCodeSms(mobileVal: String): JsCmd = {
      WebHelpers.realMobile(Full(mobileVal)) match {
        case Full(mb) =>
          if (isRegUser(mb)) {
            return WebHelpers.formError("regMobile", "此手机号已经注册，请登录！")
          }

          if (SmsHelpers.getSendSmsCount(mb) >= WebCacheHelper.getSmsCountLimit()) {
            return WebHelpers.formError("regMobile", s"对不起，此手机号获取验证码已超出每天${WebCacheHelper.getSmsCountLimit()}条")
          }

          val cacheTime = SmsHelpers.smsCode(mb).cacheTime
          WebHelpers.removeFormError() & (if ((WebHelpers.now - cacheTime) > 60) {
            SmsHelpers.sendCodeSms("注册牛标用户", mb)
            JsRaw("""$("#getCodeBtn").countdown();$("#msg-tip").hide().text("")""")
          } else {
            JsRaw("""$("#msg-tip").show().text("验证码已经发送至%s，请查看短信获取！")""".format(mb))
          }) & Alert(SmsHelpers.smsCode(mb).code)
        case _ => return WebHelpers.formError("regMobile", "请输入正确的手机号！")
      }
    }

    "@regMobile" #> text(mobile, mobile = _) &
      "@regCode" #> text(code, code = _) &
      "@regPwd" #> password(pwd, pwd = _) &
      "@getCodeBtn [onclick]" #> ajaxCall(ValById("regMobile"), sendCodeSms) &
      "type=submit" #> ajaxSubmit("确认注册", process)
  }

  def forgot = {
    var mobile, code, pwd = ""

    def process(): JsCmd = {
      WebHelpers.realMobile(Full(mobile)) match {
        case Full(mb) =>
          User.find(By(User.mobile, mb)) match {
            case Full(user) =>
              if (code.trim().isEmpty()) {
                return WebHelpers.formError("forgotCode", "请输入短信验证码或密码！")
              }

              val smsCode = SmsHelpers.smsCode(mb).code
              if (smsCode != code) {
                return WebHelpers.formError("forgotCode", "短信验证码错误，请确认！")
              }

              if (pwd.trim().isEmpty()) {
                return WebHelpers.formError("forgotPwd", "请设置您的新密码！")
              }
              user.password(pwd)
              user.save()
              WebHelpers.showLoginModal("login-panel")
            case _ =>
              return WebHelpers.formError("forgotMobile", "此手机号未注册，无法找回密码！")
          }
        case _ => return WebHelpers.formError("forgotMobile", "请输入正确的手机号！")
      }
    }

    def sendCodeSms(mobileVal: String): JsCmd = {
      WebHelpers.realMobile(Full(mobileVal)) match {
        case Full(mb) =>
          if (!isRegUser(mb)) {
            return WebHelpers.formError("forgotMobile", "此手机号未注册，无法找回密码！")
          }
          
         if (SmsHelpers.getSendSmsCount(mb) >= WebCacheHelper.getSmsCountLimit()) {
            return WebHelpers.formError("forgotMobile", s"对不起，此手机号获取验证码已超出每天${WebCacheHelper.getSmsCountLimit()}条")
          }

          val cacheTime = SmsHelpers.smsCode(mb).cacheTime
          WebHelpers.removeFormError() & (if ((WebHelpers.now - cacheTime) > 60) {
            SmsHelpers.sendCodeSms("牛标找回密码", mb)
            JsRaw("""$("#forgotGetCodeBtn").countdown();$("#msg-tip").hide().text("")""")
          } else {
            JsRaw("""$("#msg-tip").show().text("验证码已经发送至%s，请查看短信获取！")""".format(mb))
          }) & Alert(SmsHelpers.smsCode(mb).code)
        case _ => return WebHelpers.formError("forgotMobile", "请输入正确的手机号！")
      }
    }

    "@forgotMobile" #> text(mobile, mobile = _) &
      "@forgotCode" #> text(code, code = _) &
      "@forgotPwd" #> password(pwd, pwd = _) &
      "@forgotGetCodeBtn [onclick]" #> ajaxCall(ValById("forgotMobile"), sendCodeSms) &
      "type=submit" #> ajaxSubmit("确认修改", process)
  }
}