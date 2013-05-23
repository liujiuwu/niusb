package code.lib

import net.liftweb.http.js.JE.JsRaw

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

}