package com.niusb.util

import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.util.Helpers._

object BootBoxHelpers extends BootBoxHelpers

trait BootBoxHelpers {
  def setLocale(locale: String = "zh_CN") = s"bootbox.setLocale('${locale}');"

  case class BoxConfirm(text: String, yes: JsCmd, locale: String = "zh_CN") extends JsCmd {
    def toJsCmd = setLocale(locale) + "bootbox.confirm(" + text.encJs + ",function(result){if(result){" + yes.toJsCmd + "}})"
  }

  case class BoxAlert(text: String, click: JsCmd = Noop, locale: String = "zh_CN") extends JsCmd {
    def toJsCmd = setLocale(locale) + "bootbox.alert(" + text.encJs + ",function(){" + click.toJsCmd + "}" + ")"
  }
}

