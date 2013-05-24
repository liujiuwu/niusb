package code.lib

import scala.xml.NodeSeq
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.util.Helpers._
import net.liftweb.http.js.jquery.JqJsCmds._
import net.liftweb.http.js.jquery.JqJsCmds.JqSetHtml

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

}