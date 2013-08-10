package code.snippet

import scala.xml.Text
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.util.Helpers._
import net.liftweb.http.S
import net.liftweb.http.S._
import net.liftweb.util._
import net.liftweb.mapper._
import net.liftweb.util.Helpers._
import code.model.Brand
import code.model.BrandType

object MarketOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "index" => list
    case "brandTypes" => brandTypes
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

  def brandTypes = {
    val bts = BrandType.getBrandTypes().values.toList
    ".brand-types li" #> bts.map(b => {
      "li *" #> <a href={ "/market/index?type=" + b.code }>{ b.code + "." + b.name }</a>
    })
  }

}