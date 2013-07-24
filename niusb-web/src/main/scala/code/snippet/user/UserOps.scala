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
import net.liftweb.mapper.By
import code.model.UserType
import scala.xml.NodeSeq

object UserOps {
  def edit = {
    val user = User.currentUser.get
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
    val user = User.currentUser.get
    var (code, pwd) = ("", "")
    def process(): JsCmd = {
      if (pwd.isEmpty() || pwd.length() < 6) {
        return WebHelper.formError("pwd", "新密码不能为空，且不少于6位字符。")
      }

      if (code.isEmpty()) {
        return WebHelper.removeFormError("pwd") & WebHelper.formError("code", "请填写正确的验证码!")
      }

      user.password(pwd)
      WebHelper.removeFormError("pwd") & WebHelper.removeFormError("code") & JsRaw(WebHelper.succMsg("opt_pwd_tip", Text(if (user.save) "密码修改成功！" else "密码修改失败！")))
    }

    "@code" #> password(code, code = _) &
      "@pwd" #> password(pwd, pwd = _) &
      "@sub" #> hidden(process)
  }

}