package code.lib

import java.awt.Color
import java.awt.Font
import java.awt.GradientPaint
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Random

import scala.math.abs
import scala.xml.NodeSeq

import javax.imageio.ImageIO
import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.http.InMemoryResponse
import net.liftweb.http.LiftResponse
import net.liftweb.http.S
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.jquery.JqJsCmds.FadeOut
import net.liftweb.http.js.jquery.JqJsCmds.JqSetHtml
import net.liftweb.http.js.jquery.JqJsCmds.Show
import net.liftweb.http.js.jquery.JqJsCmds.jsExpToJsCmd
import net.liftweb.util.Helpers.TimeSpan
import net.liftweb.util.Helpers.intToTimeSpanBuilder
import net.liftweb.util.Helpers.strToSuperArrowAssoc

object WebHelper {
  def oddOrEven(current: String) = {
    current match {
      case "odd" => "even"
      case _ => "odd"
    }
  }

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

  def fmtDateStr(date: java.util.Date) = {
    date match {
      case null => ""
      case _ => val format = new SimpleDateFormat("yyyy-MM-dd"); format.format(date)
    }
  }

  def dateParse(s: String): Box[java.util.Date] = {
    val df = new SimpleDateFormat("yyyy-MM-dd")
    try {
      val date = df.parse(s)
      Full(date)
    } catch {
      case _: Exception => Empty
    }
  }

  def captcha(): Box[LiftResponse] = {
    Full(InMemoryResponse(generateCaptchaData, List("Content-Type" -> "image/png"), Nil, 200))
  }

  private def generateCaptchaData = {
    val width = 150
    val height = 50

    val data =
      Array(Array('z', 'e', 't', 'c', 'o', 'd', 'e'),
        Array('l', 'i', 'n', 'u', 'x'),
        Array('f', 'r', 'e', 'e', 'b', 's', 'd'),
        Array('u', 'b', 'u', 'n', 't', 'u'),
        Array('j', 'e', 'e'))

    val bufferedImage = new BufferedImage(width, height,
      BufferedImage.TYPE_INT_RGB)

    val g2d = bufferedImage.createGraphics()
    val font = new Font("Arial", Font.BOLD, 18)
    g2d.setFont(font)

    val rh = new RenderingHints(
      RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON)

    rh.put(RenderingHints.KEY_RENDERING,
      RenderingHints.VALUE_RENDER_QUALITY)

    g2d.setRenderingHints(rh)

    val gp = new GradientPaint(0, 0,
      Color.red, 0, height / 2, Color.black, true)

    g2d.setPaint(gp)
    g2d.fillRect(0, 0, width, height)

    g2d.setColor(new Color(255, 153, 0))

    val r = new Random()
    val index = abs(r.nextInt()) % 5

    val captcha = String.copyValueOf(data(index))
    S.setSessionAttribute("captcha", captcha)

    var x = 0
    var y = 0

    for (i <- 0 until data(index).length) {
      x += 10 + (abs(r.nextInt()) % 15)
      y = 20 + abs(r.nextInt()) % 20
      g2d.drawChars(data(index), i, 1, x, y)
    }

    g2d.dispose()
    val baos = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "png", baos)
    val captchaData = baos.toByteArray()
    baos.close

    captchaData
  }

}