package code.snippet

import scala.xml.NodeSeq
import scala.xml.Text
import net.liftweb.http.PaginatorSnippet
import net.liftweb.http.S._
import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import net.liftweb.http.RequestVar
import net.liftweb.common.Box
import net.liftweb.common.Empty

trait TabMenu {
  object tabMenuRV extends RequestVar[Box[(String, String)]](Empty)

  def tabMenu = {
    val menu: NodeSeq = tabMenuRV.get match {
      case Full(t) => <li class="active"><a>{ if (!t._1.isEmpty()) <i class={ "icon-" + t._1 }></i> }{ Text(" ") }{ t._2 }</a></li>
      case _ => Text("")
    }
    "span" #> menu
  }
}