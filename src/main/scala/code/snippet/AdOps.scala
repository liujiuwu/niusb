package code.snippet

import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import code.model.AdSpace
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import scala.xml._
import net.liftweb.common._
import net.liftweb.util.CssSel
import code.lib.WebCacheHelper

object AdOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "index" => index
  }

  def index = {
    val code = S.attr("code") match {
      case Full(c) => c.toInt
      case _ => 0
    }

    WebCacheHelper.adSpaces.get(code.toInt) match {
      case Some(adSpace) =>
        "li" #> adSpace.ads.map { ad =>
          "img [src]" #> ad.pic &
            "img [height]" #> adSpace.height &
            "img [width]" #> adSpace.width
        }
      case _ => "*" #> Text("无广告图")
    }
  }

}