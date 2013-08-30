package code.snippet.admin

import scala.xml.NodeSeq
import net.liftweb.http.DispatchSnippet
import code.model.WebSet
import net.liftweb.common.Box
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml._

object WebSetOps extends DispatchSnippet {
  val dispatch: DispatchIt = {
    case "edit" => edit _
  }

  def edit(nodeSeq: NodeSeq): NodeSeq = {
    <b></b>
  }
}