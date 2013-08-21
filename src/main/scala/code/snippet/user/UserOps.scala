package code.snippet.user

import scala.xml.Text
import code.lib.WebHelper
import code.model.User
import net.liftweb.common._
import net.liftweb.http.RequestVar
import net.liftweb.http.S
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper.Genders
import net.liftweb.util.Helpers._
import code.lib.WebHelper._
import net.liftweb.mapper.By
import code.model.UserType
import scala.xml.NodeSeq
import code.snippet.SnippetHelper
import code.lib.MemcachedHelper
import code.lib.SmsCode
import code.lib.SmsHelper
import net.liftweb.http.DispatchSnippet
import code.lib.BoxAlert

object UserOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "edit" => edit
    case "updatePwd" => updatePwd
  }

  private def user = User.currentUser.openOrThrowException("not found user")

  def edit = {
    def process(): JsCmd = {
      user.save
      JsRaw(WebHelper.succMsg("opt_profile_tip", Text("个人信息保存成功！"))) & JsRaw("""$("#displayName").text("%s")""".format(user.displayName))
    }

    "@name" #> text(user.name.is, user.name(_)) &
      "@gender" #> selectObj[Genders.Value](Genders.values.toList.map(v => (v, v.toString)), Full(user.gender.is), user.gender(_)) &
      "@user_type" #> selectObj[UserType.Value](UserType.values.toList.map(v => (v, v.toString)), Full(user.userType.is), user.userType(_), "disabled" -> "disabled") &
      "@qq" #> text(user.qq.is, user.qq(_)) &
      "@phone" #> text(user.phone.is, user.phone(_)) &
      "@email" #> text(user.email.is, user.email(_)) &
      "@address" #> text(user.address.is, user.address(_)) &
      "@sub" #> hidden(process)
  }

  def updatePwd = {
    var (code, pwd) = ("", "")
    def process(): JsCmd = {
      if (pwd.trim.isEmpty() || pwd.trim.length() < 6) {
        return formError("pwd", "新密码不能为空，且不少于6位字符。")
      }

      if (code.trim.isEmpty()) {
        return removeFormError() & formError("code", "请填写正确的短信验证码或旧密码!")
      }

      removeFormError() & (if (user.authSmsCodeOrPwd(code)) {
        user.password(pwd)
        user.save()
        BoxAlert("密码修改成功，请牢记！", Reload)
      } else {
        formError("code", "验证码或旧密码错误，请确认!")
      })
    }

    def sendCodeSms(): JsCmd = {
      val mobile = user.mobile.get
      val cacheTime = SmsHelper.smsCode(mobile)._2
      removeFormError() & (if ((WebHelper.now - cacheTime) > 60) {
        SmsHelper.sendCodeSms(mobile)
        JsRaw("""$("#getCodeBtn").countdown();$("#opt_pwd_tip").hide().text("")""")
      } else {
        JsRaw("""$("#opt_pwd_tip").show().text("验证码已经发送至%s，请查看短信获取！")""".format(mobile))
      }) & Alert(SmsHelper.smsCode(user.mobile.get)._1)
    }

    "@code" #> password(code, code = _) &
      "@pwd" #> password(pwd, pwd = _) &
      "@getCodeBtn [onclick]" #> ajaxInvoke(sendCodeSms) &
      "type=submit" #> ajaxSubmit("保存", process)
  }

}