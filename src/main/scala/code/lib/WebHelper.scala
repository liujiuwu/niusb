package code.lib

import scala.xml.NodeSeq
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.util.Helpers._
import net.liftweb.http.js.jquery.JqJsCmds._
import net.liftweb.http.js.jquery.JqJsCmds.JqSetHtml
import net.liftweb.common.Full
import net.liftweb.common.Box
import net.liftweb.common.Empty

object WebHelper {

  def removeFormErrors(fieldNames: List[String]) = {
    for (fieldName <- fieldNames) removeFormError(fieldName)
  }

  def removeFormError(fieldName: String) = {
    JsRaw("""$("#group_%1$s").removeClass("success error warning");$("#error_%1$s").text("%2$s")""" format (fieldName, ""))
  }

  def formErrors(errors: Map[String, String]) = {
    for ((fieldName, errorMsg) <- errors) formError(fieldName, errorMsg)
  }

  def formError(fieldName: String, msg: String) = {
    JsRaw("""$("#group_%1$s").removeClass("success error warning");$("#group_%1$s").addClass("error");$("#error_%1$s").text("%2$s")""" format (fieldName, msg))
  }

  def succMsg(where: String, msg: NodeSeq, cssClass: String = "alert-success", duration: TimeSpan = 0 second, fadeTime: TimeSpan = 2 second): JsCmd = {
    (Show(where) & JqSetHtml(where, msg) & JsRaw("""$("#%s").removeClass("alert-error alert-success")""".format(where, cssClass)) & JsRaw("""$("#%s").addClass("%s")""".format(where, cssClass)) & FadeOut(where, duration, fadeTime))
  }

  def errorMsg(where: String, msg: NodeSeq, cssClass: String = "alert-error", duration: TimeSpan = 0 second, fadeTime: TimeSpan = 3 second): JsCmd = {
    succMsg(where, msg, cssClass, duration, fadeTime)
  }

  def getRealMobile(mobile: Box[String]): Box[String] = {
    val mobileRegx = """^(13[0-9]|15[0|3|6|7|8|9]|18[8|9])(\d{8})$""".r
    mobile.openOr(Empty) match {
      case mobileRegx(mp, ms) => Full(mp + ms)
      case _ => Empty
    }
  }

}