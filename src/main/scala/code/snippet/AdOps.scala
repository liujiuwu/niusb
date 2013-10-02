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
import scala.util.Try
import scala.util.Success

object AdOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "index" => index
    case "friendlyLink" => friendlyLink
  }

  def index = {
    val code = S.attr("code").map(_.toInt).openOr(0)
    WebCacheHelper.adSpaces.get(code) match {
      case Some(adSpace) =>
        "li" #> adSpace.ads.map { ad =>
          "a [href]" #> { if (ad.link.is == null || ad.link.is.isEmpty()) (None: Option[String]) else Option(ad.link.is) } &
          "a [target]" #> { if (ad.link.is == null || ad.link.is.isEmpty()) (None: Option[String]) else Option("_blank") } &
            "img [src]" #> ad.pic &
            "img [height]" #> adSpace.height &
            "img [width]" #> adSpace.width
        }
      case _ => "*" #> Text("无广告图")
    }
  }

  def friendlyLink = {
    val code = S.attr("code").map(_.toInt).openOr(2)
    WebCacheHelper.adSpaces.get(code) match {
      case Some(adSpace) =>
        "li" #> adSpace.ads.map { ad =>
          "a [href]" #> ad.link.is &
            "a *" #> ad.title.is
        }
      case _ => "*" #> Text("无广告图")
    }

    /*<ul data-lift="AdOps.friendlyLink?code=2" class="list-inline text-center">
		<li><a href="/about" target="_blank"></a></li>
	</ul>*/
  }

}