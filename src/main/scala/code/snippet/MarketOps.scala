package code.snippet

import scala.xml.Text
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.util.Helpers._
import net.liftweb.http.S
import net.liftweb.http.S._
import net.liftweb.util._
import net.liftweb.mapper._
import code.model.Brand

object MarketOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "index" => list
    case "view" => view
  }

  def list = {
    "" #> Text("")
  }

  def view = {
    (for {
      brandId <- S.param("id").flatMap(asLong) ?~ "商标ID不存在或无效"
      brand <- Brand.find(By(Brand.id, brandId)) ?~ s"ID为${brandId}的商标不存在。"
    } yield {
      "*" #> brand.name
    }): CssSel
  }

}