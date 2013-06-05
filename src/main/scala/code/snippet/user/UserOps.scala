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

object UserOps {
  object userVar extends RequestVar[User](User.currentUser.get)

  def update = {
    User.currentUser match {
      case Full(user) => userVar(User.find(By(User.id, user.id.get)).openOrThrowException("用户不存在%s".format(user.id.get)))
      case _ => S.redirectTo("/")
    }

    val user = userVar.get
    def process(): JsCmd = {
      if (user.save) {
        val name = if (user.name.get == "") user.mobile.get else user.name.get
        JsRaw(WebHelper.succMsg("opt_profile_tip", Text("个人信息保存成功！"))) & JsRaw("""$("#topBarUserName").text("%s")""".format(name))
      } else {
        JsRaw(WebHelper.errorMsg("opt_profile_tip", Text("个人信息保存失败！")))
      }
    }

    "@name" #> text(user.name.get, user.name(_)) &
      "@gender" #> selectObj[Genders.Value](Genders.values.toList.map(v => (v, v.toString)), Full(user.gender.get), user.gender(_)) &
      "@qq" #> text(user.qq.get, user.qq(_)) &
      "@phone" #> text(user.phone.get, user.phone(_)) &
      "@email" #> text(user.email.get, user.email(_)) &
      "@address" #> text(user.address.get, user.address(_)) &
      "@sub" #> hidden(process)
  }

  def updatePwd = {
    User.currentUser match {
      case Full(user) => userVar(User.find(By(User.id, user.id.get)).openOrThrowException("用户不存在%s".format(user.id.get)))
      case _ => S.redirectTo("/")
    }

    var code = ""
    var pwd = ""
    val user = userVar.get
    def process(): JsCmd = {
      if (pwd == "" || pwd.length() < 6) {
        return WebHelper.formError("pwd", "新密码不能为空，且不少于6位字符。")
      }

      if (code == "") {
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