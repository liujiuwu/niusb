package code.snippet

import net.liftweb.http.DispatchSnippet
import net.liftweb.util.Helpers._
import scala.xml.NodeSeq

object HelloWorld extends DispatchSnippet {
  def dispatch = {
    case name => render(name)_
  }

  def render(name: String)(ignore: NodeSeq): NodeSeq = {
    list(ignore)
  }

  def list = {
    "*" #> <span>===================</span>
  }
}