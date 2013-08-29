package code.snippet

import scala.xml.Text

import code.lib.WebCacheHelper
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.util.Helpers.strToCssBindPromoter

object WendaOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "view" => view
  }

  def view = {
    "" #> Text("")
  }

}