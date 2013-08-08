package code.snippet

import scala.xml.Text
import code.lib.BrandTypeHelper
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.http.S
import net.liftweb.common._
import code.model.Brand
import net.liftweb.mapper._
import code.lib.SyncData

object IndexOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "brandTypes" => brandTypes
    case "tabConent" => tabConent
  }

  def brandTypes = {
    val brandTypes = BrandTypeHelper.brandTypes
    "*" #> Text("")
  }

  def tabConent = {
    S.attr("tabIdx") match {
      case Full(idx) => brandDatas(idx)
      case _ => "*" #> Text("")
    }
  }

  def brandDatas(idx: String) = {
    var brands = List.empty[Brand]
    idx match {
      case "0" =>
        brands = Brand.findAll(MaxRows[Brand](21), OrderBy(Brand.id, Descending))
      case "1" =>
        brands = Brand.findAll(StartAt(30), MaxRows[Brand](21), OrderBy(Brand.id, Descending))
      case "2" =>
        brands = Brand.findAll(StartAt(0), MaxRows[Brand](21), OrderBy(Brand.createdAt, Descending))
      case "3" =>
        brands = Brand.findAll(StartAt(0), MaxRows[Brand](21), OrderBy(Brand.name, Descending))
    }
    "li" #> brands.map(brand => {
      ".m-brand-img *" #> <img src={ brand.displayPicSrc() }/> &
        ".m-brand-name *" #> brand.name

    })
  }
}