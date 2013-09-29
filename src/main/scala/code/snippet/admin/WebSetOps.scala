package code.snippet.admin

import scala.xml.NodeSeq
import net.liftweb.http.DispatchSnippet
import net.liftweb.common._
import net.liftweb.http.SHtml._
import code.model.Webset
import net.liftweb.util.Helpers._
import scala.util.Try
import scala.util.Success
import net.liftweb.http.S
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import com.niusb.util.WebHelpers
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds._
import code.lib.WebCacheHelper
import net.liftweb.http.js.JE

object WebSetOps extends DispatchSnippet {
  val dispatch: DispatchIt = {
    case "edit" => edit
  }

  def edit = {
    val webset = Webset.find().headOption match {
      case Some(w) => w
      case _ => Webset.create
    }

    var basePriceFloat, smsCountLimit = ""

    def process(): JsCmd = {
      Try(basePriceFloat.trim().toInt) match {
        case Success(v) => webset.basePriceFloat(v)
        case _ => return WebHelpers.formError("basePriceFloat", "商标基价浮动百分比必须是数字")
      }

      Try(smsCountLimit.trim().toInt) match {
        case Success(v) => webset.smsCountLimit(v)
        case _ => return WebHelpers.formError("smsCountLimit", "每天每手机号短信验证码获取数量限制必须是数字")
      }
      webset.save()
      WebCacheHelper.loadWebsets()
      WebHelpers.removeFormError() & WebHelpers.alertSuccess("保存设置成功！")
    }

    "@basePriceFloat" #> text(webset.basePriceFloat.is.toString(), basePriceFloat = _) &
      "@smsCountLimit" #> text(webset.smsCountLimit.is.toString(), smsCountLimit = _) &
      "type=submit" #> ajaxSubmit("保存设置", process)
  }
}