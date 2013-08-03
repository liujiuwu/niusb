package code.snippet

import net.liftweb.common._
import net.liftweb.http.DispatchSnippet
import net.liftweb.util.Helpers._
import scala.xml.Text
import code.lib.BrandTypeHelper

object IndexOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "brandTypes" => brandTypes
  }

  def brandTypes = {
    val brandTypes = BrandTypeHelper.brandTypes
    "*" #> Text("")
  }
}