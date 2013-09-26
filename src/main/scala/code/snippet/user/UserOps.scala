package code.snippet.user

import com.niusb.util.SmsHelpers
import com.niusb.util.WebHelpers
import com.niusb.util.WebHelpers.formError

import code.model.User
import code.snippet.SnippetHelper
import net.liftweb.common._
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmd._
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper._
import net.liftweb.util.Helpers._

object UserOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "edit" => edit
    case "updatePwd" => updatePwd
  }

  def edit = {
    var name = ""
    val user = User.find(By(User.id, loginUser.id.is)).get
    def process(): JsCmd = {
      if (!name.trim().isEmpty() && (name.length() < 2 || name.length() > 10)) {
        return formError("name", "请填写真实姓名，长度介于2~10字!")
      } else {
        user.name(name)
      }
      user.save
      WebHelpers.alertSuccess("个人信息保存成功", true)
    }

    "@name" #> text(user.name.is, name = _) &
      "@gender" #> selectObj[Genders.Value](Genders.values.toList.map(v => (v, v.toString)), Full(user.gender.is), user.gender(_)) &
      "#user_type *" #> user.userType.asHtml &
      "@qq" #> text(user.qq.is, user.qq(_)) &
      "@phone" #> text(user.phone.is, user.phone(_)) &
      "@email" #> text(user.email.is, user.email(_)) &
      "@address" #> text(user.address.is, user.address(_)) &
      "type=submit" #> ajaxSubmit("保存修改", process)
  }

  def updatePwd = {
    val user = User.find(By(User.id, loginUser.id.is)).get
    var pwd, newPwd = ""
    def process(): JsCmd = {
      if (pwd.trim.isEmpty()) {
        return formError("pwd", "请输入您的旧密码!")
      } else if (!user.password.match_?(pwd)) {
        return formError("pwd", "旧密码错误，请确认!")
      }

      if (newPwd.trim.isEmpty() || newPwd.trim.length() < 6) {
        return formError("newPwd", "新密码不能为空，且不少于6位字符。")
      } else {
        user.password(newPwd)
        user.save
      }
      WebHelpers.alertSuccess("密码修改成功，请妥善保管您的新密码！", true)
    }

    "@pwd" #> password(pwd, pwd = _) &
      "@newPwd" #> password("", newPwd = _) &
      "type=submit" #> ajaxSubmit("确认修改", process)
  }

}