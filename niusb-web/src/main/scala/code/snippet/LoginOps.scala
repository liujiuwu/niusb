package code.snippet

import code.lib.WebHelper
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

object LoginOps {
  private object mobileVar extends RequestVar[Box[String]](Empty)
  private object pwdVar extends RequestVar[Box[String]](Empty)
  
  def login = {
    def process(): JsCmd = {
      WebHelper.getRealMobile(mobileVar.get) match {
        case Full(mobile) =>
          User.find(By(User.mobile, mobile)) match {
            case Full(user) =>
              User.logUserIn(user)
              S.redirectTo("/")
            case _ =>
              val user = new User
              user.mobile(mobile)
              if (user.save) {
                User.logUserIn(user)
                S.redirectTo("/")
              } else {
                JsRaw(WebHelper.errorMsg("opt_login_tip", Text("注册失败，请稍候重试！")))
              }
          }
        case _ => WebHelper.formError("mobile", "错误的手机号！")
      }
    }

    "@mobile" #> ajaxText(mobileVar.get.openOr(""), mobile => {
      WebHelper.getRealMobile(Box.legacyNullTest(mobile)) match {
        case Full(m) =>
          mobileVar(Full(m))
          WebHelper.removeFormError("mobile") & JsRaw("""$("#login_btn").removeClass("disabled")""") & JsRaw("""$("#getCode_btn").removeClass("disabled")""")
        case _ => JsRaw("""$("#getCode_btn").addClass("disabled")""") & JsRaw("""$("#login_btn").addClass("disabled")""") & WebHelper.formError("mobile", "错误的手机号！")
      }
    }) &
      "@pwd" #> password(pwdVar.get.openOr(""), pwd => pwdVar(Box.legacyNullTest(pwd))) &
      "@sub" #> hidden(process)
  }
}