package com.niusb.util

import net.liftweb.util.Helpers
import net.liftweb.common._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JE.JsRaw._
import net.liftweb.http.LiftResponse
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmd._
import net.liftweb.http.js.JsCmds._
import java.awt.image._
import javax.imageio.ImageIO
import java.text.SimpleDateFormat
import scala.xml.NodeSeq
import net.liftweb.util.Helpers._
import net.liftweb.http.InMemoryResponse
import net.liftweb.http.S
import scala.util.Random
import java.io.ByteArrayOutputStream
import scala.language.postfixOps
import scala.math.abs
import scala.xml.NodeSeq
import java.awt.RenderingHints
import java.awt.GradientPaint
import java.awt.Font
import java.awt.Color
import net.liftweb.http.js.jquery.JqJsCmds.FadeOut

object WebHelpers extends WebHelpers with BootBoxHelpers {
}

trait WebHelpers {
  val df = new SimpleDateFormat("yyyy-MM-dd")
  val dfShortTime = new SimpleDateFormat("yyyy-MM-dd HH:mm")
  val dfLongTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  val WebSiteUrlAndName = ("http://www.niusb.com", "牛标网")

  case class CacheValue[T](compute: () => T, lifespanInMillis: Long) {
    private var currentValue: Box[T] = Empty
    private var lastCalc: Long = 0
    def get: T = synchronized {
      if (lastCalc + lifespanInMillis < Helpers.millis) {
        currentValue = Empty
      }
      currentValue match {
        case Full(v) => v
        case _ => {
          val ret = compute()
          lastCalc = Helpers.millis
          currentValue = Full(ret)
          ret
        }
      }
    }
  }

  object TrueOrFalse extends Function1[String, Boolean] {
    lazy val selectTrueOrFalse = Seq("0" -> "否", "1" -> "是")
    def apply(v: String) = v match {
      case "0" => false
      case "1" => true
    }
  }

  object TrueOrFalse2Str extends Function1[Boolean, Box[String]] {
    def apply(v: Boolean) = v match {
      case false => Full("0")
      case true => Full("1")
    }
  }

  def now = (System.currentTimeMillis() / 1000).toInt

  def badge(label: String, data: AnyVal, prefix: String = "￥") = <span class={ "badge badge-" + label }>{ prefix }{ data }</span>

  def oddOrEven(current: String) = {
    current match {
      case "odd" => "even"
      case _ => "odd"
    }
  }

  def removeFormError(fieldName: String = "") = {
    if (fieldName.trim.isEmpty()) {
      JsRaw("""$(".form-group").removeClass("has-success has-error has-warning");$("form .help-block").html("")""")
    } else {
      JsRaw("""$("#form-%1$s").removeClass("has-success has-error has-warning");$("#form-%1$s .help-block").hide().text("%2$s")""" format (fieldName, ""))
    }
  }

  def formError(fieldName: String, msg: String) = {
    JsRaw("""$(".form-group").removeClass("has-success has-error has-warning");$("form .help-block").html("")""") &
      JsRaw("""$("#%1$s").focus()""".format(fieldName)) &
      JsRaw("""$("#form-%1$s").removeClass("has-success has-error has-warning");$("#form-%1$s").addClass("has-error");$("#form-%1$s .help-block").show().html("%2$s")""" format (fieldName, msg))
  }

  /* def succMsg(where: String, msg: NodeSeq, cssClass: String = "alert-success", duration: TimeSpan = 0 second, fadeTime: TimeSpan = 2 second): JsCmd = {
    (Show(where) & JqSetHtml(where, msg) & JsRaw("""$("#%s").removeClass("alert-error alert-success")""".format(where, cssClass)) & JsRaw("""$("#%s").addClass("%s")""".format(where, cssClass)) & FadeOut(where, duration, fadeTime))
  }

  def errorMsg(where: String, msg: NodeSeq, cssClass: String = "alert-error", duration: TimeSpan = 0 second, fadeTime: TimeSpan = 3 second): JsCmd = {
    succMsg(where, msg, cssClass, duration, fadeTime)
  }*/

  def realMobile(mobile: Box[String]): Box[String] = {
    mobile match {
      case Full(m) if (!m.trim().isEmpty()) =>
        val mobileRegx = """^(13[0-9]|14[0-9]|15[0-9]|18[0-9])(\d{8})$""".r
        m match {
          case mobileRegx(mp, ms) => Full(mp + ms)
          case _ => Empty
        }
      case _ => Empty
    }
  }

  def fmtDateStr(date: java.util.Date, fmt: SimpleDateFormat = df) = {
    date match {
      case null => ""
      case _ => fmt.format(date)
    }
  }

  def dateParse(s: String): Box[java.util.Date] = {
    try {
      val date = df.parse(s)
      Full(date)
    } catch {
      case _: Exception => Empty
    }
  }

  def memKey(ip: String, module: String, flag: String, otherFlag: String = "") = {
    s"${ip}_${module}_${flag}" + (if (!otherFlag.isEmpty()) "_" + otherFlag else "")
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

  def options(value: String, label: String, selected: String, prependValue: Boolean = false) = {
    <option value={ value } selected={ if (selected == value) "selected" else null }>{ if (prependValue) value + "." + label else label }</option>
  }

  def alert(title: String, message: String, cls: String, isReload: Boolean = false, duration: TimeSpan = 1 second, byId: String = "alert-msg"): JsCmd = {
    val alertHtml = <div class={ "alert alert-block " + cls + "  fade in" }>
                      <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
                      {
                        if (!title.isEmpty())
                          <h4> { title } </h4>
                      }
                      <p>{ message }</p>
                    </div>

    SetHtml(byId, alertHtml) & JsShowId(byId) & (if (isReload) After(duration, Reload) else FadeOut(byId, duration, 2 second))
  }

  def alertSuccess(message: String): JsCmd = {
    alert("", message, "alert-success")
  }

  def alertError(message: String): JsCmd = {
    alert("", message, "alert-danger")
  }

  def alertWarning(message: String): JsCmd = {
    alert("", message, "alert-warning")
  }

  def alertInfo(message: String): JsCmd = {
    alert("", message, "alert-info")
  }

  def showLoginModal(action: String): JsCmd = {
    JsRaw("""$("#loginDialog").modal({marginTop:80})""") &
      JsRaw("""$('#loginDialogTab a[href="#%s"]').tab('show')""".format(action))
  }
}