package code.snippet.user

import code.model.User
import net.liftweb.common._
import net.liftweb.http.RequestVar
import net.liftweb.http.S
import net.liftweb.http.SHtml._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.jquery.JqJsCmds._
import net.liftweb.mapper.Genders
import net.liftweb.util.Helpers._
import scala.xml.Text


object UserOps {
  object userVar extends RequestVar[User](User.currentUser.get)

  def update = {
    def process(): JsCmd = {
      val user = userVar.get
      user.validate match {
        case Nil =>
          user.save
          DisplayMessage("opt_tip",Text("个人信息保存成功！"),1 second, 3 seconds)
           //SetHtml("opt_tip", Text("个人信息保存成功！")) & FadeIn("opt_tip", 0 second, 1 second) & FadeOut("opt_tip", 0 second, 3 seconds)
          //JsRaw("""$("#opt_tip").text("个人信息保存成功！");$("#opt_tip").addClass("success");$("#opt_tip").fadeIn("slow");$("#opt_tip").fadeOut("slow")""")
        case errors =>
          //JsRaw("""$("#opt_tip").text("个人信息保存失败！");$("#opt_tip").addClass("error")""")
          //SetHtml("opt_tip", Text("个人信息保存成功！")) & FadeIn("opt_tip", 0 second, 1 second) & FadeOut("opt_tip", 0 second, 3 seconds)
          Noop
      }
    }

    User.currentUser match {
      case Full(user) => userVar(user)
      case _ => S.redirectTo("/")
    }

    val user = userVar.get
    "@name" #> text(user.name.is, user.name(_)) &
      "@gender" #> selectObj[Genders.Value](Genders.values.toList.map(v => (v, v.toString)), Full(user.gender.get), user.gender(_)) &
      "@qq" #> text(user.qq.is, user.qq(_)) &
      "@phone" #> text(user.phone.is, user.phone(_)) &
      "@email" #> text(user.email.is, user.email(_)) &
      "@address" #> text(user.address.is, user.address(_)) &
      "@sub" #> hidden(process)
  }

}