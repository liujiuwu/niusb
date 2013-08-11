package code.snippet

import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import code.model.AdSpace
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import scala.xml._
import net.liftweb.common._
import net.liftweb.util.CssSel

object AdOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "index" => index
  }

  def index = {
    val code = S.attr("code") match {
      case Full(c) => c.toInt
      case _ => 0
    }
    val adSpace = AdSpace.findByCode(code).get
    "li" #> adSpace.ads.map { ad =>
      "img [src]" #> ad.pic &
      "img [height]" #> adSpace.height &
      "img [width]" #> adSpace.width
    }
  }

}