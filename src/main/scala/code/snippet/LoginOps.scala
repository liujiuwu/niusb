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
import code.lib.WebHelper
import code.lib.SmsCode

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

      pwdBox match {
        case Full(pwd) if (!pwd.isEmpty()) =>
          val code = MemcachedHelper.get(mobile) match {
            case Some(code) => code
            case _ => ""
          }

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
        case _ => removeFormError("mobile") & formError("pwd", "请输入验证码或密码！")
      }

    }

    def sendCodeSms(mobileVal: String): JsCmd = {
      val mobile = realMobile(Full(mobileVal)) match {
        case Full(mb) => mb
        case _ => return formError("mobile", "请输入正确的手机号！")
      }

      val cacheTime = MemcachedHelper.get(mobile) match {
        case Some(sc) =>
          val smsCode = sc.asInstanceOf[SmsCode]
          smsCode.cacheTime
        case _ => 0
      }

      if ((WebHelper.now - cacheTime) > 60) {
        SmsHelper.sendCodeSms(mobile)
        JsRaw("smsCodeCountdown()") & JsRaw("""$("#opt_login_tip").hide().text("")""")
      } else {
        JsRaw("""$("#opt_login_tip").show().text("验证码已经发送至%s，请查看短信获取！")""".format(mobile))
      }
    }

    "@mobile" #> text(mobileBox.get, mobile => mobileBox = Full(mobile)) &
      "@pwd" #> password(pwdBox.get, pwd => pwdBox = Full(pwd)) &
      "@getCodeBtn [onclick]" #> ajaxCall(ValById("mobile"), sendCodeSms) &
      "type=submit" #> ajaxSubmit("登录", process)
  }
}