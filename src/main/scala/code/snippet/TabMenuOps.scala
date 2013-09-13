package code.snippet

import scala.xml._
import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http.DispatchSnippet

class TabMenuOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "tabMenu" => tabMenu
    case "helpNav" => helpNav
  }

  def tabMenu = {
    val menu: NodeSeq = tabMenuRV.get match {
      case Full((code, text)) if (!code.isEmpty() && !text.isEmpty()) => <li class="active"><a><i class={ "icon-" + code }></i>{ Text(" ") ++ text }</a></li>
      case _ => Text("")
    }
    "span" #> menu
  }

  def helpNav = {
    val url = originalUri
    
    "*" #> ""
  }

}