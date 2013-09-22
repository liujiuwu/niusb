package code.snippet

import net.liftweb.http.S
import scala.xml.NodeSeq
import scala.xml.Unparsed

object IEOnly {
  private def condition: String =
    S.attr("cond") openOr "IE"

  def render(ns: NodeSeq): NodeSeq =
    Unparsed("<!--[if " + condition + "]>") ++ ns ++ Unparsed("<![endif]-->")
}