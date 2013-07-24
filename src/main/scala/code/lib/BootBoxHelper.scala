package code.lib

import net.liftweb.http.js.JsCmd
import net.liftweb.util.Helpers._

object BootBoxHelper {
  def setLocale(locale: String = "zh_CN") = s"bootbox.setLocale('${locale}');"
}

case class BoxConfirm(text: String, yes: JsCmd, locale: String = "zh_CN") extends JsCmd {
  def toJsCmd = BootBoxHelper.setLocale(locale) + "bootbox.confirm(" + text.encJs + ",function(result){if(result){" + yes.toJsCmd + "}})"
}

case class BoxAlert(text: String, locale: String = "zh_CN") extends JsCmd {
  def toJsCmd = BootBoxHelper.setLocale(locale) + "bootbox.alert(" + text.encJs + ")"
}