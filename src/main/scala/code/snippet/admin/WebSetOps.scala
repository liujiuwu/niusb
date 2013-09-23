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
import net.liftweb.http.js.JsCmd
import com.niusb.util.WebHelpers
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds._

object WebSetOps extends DispatchSnippet {
  val dispatch: DispatchIt = {
    case "edit" => edit
  }

  def edit = {
    val webset = Webset.find().headOption match {
      case Some(w) => w
      case _ => Webset.create
    }

    var basePriceFloat = ""

    def process(): JsCmd = {
      Try(basePriceFloat.toInt) match {
        case Success(v) => webset.basePriceFloat(v)
        case _ => return WebHelpers.formError("basePriceFloat", "商标基价浮动百分比必须是数字")
      }
      webset.save()
      WebHelpers.alertSuccess("保存设置成功！")
    }

    "@basePriceFloat" #> text(webset.basePriceFloat.is.toString(), basePriceFloat = _) &
      "type=submit" #> ajaxSubmit("保存设置", process)
  }
}