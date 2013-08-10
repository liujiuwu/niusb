package code.snippet

import scala.xml.Text

import code.lib.BrandTypeHelper
import code.model.Brand
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.mapper.By
import net.liftweb.mapper.Descending
import net.liftweb.mapper.MaxRows
import net.liftweb.mapper.OrderBy
import net.liftweb.mapper.StartAt
import net.liftweb.util.Helpers.strToCssBindPromoter

object IndexOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "brandTypes" => brandTypes
    case "tabConent" => tabConent
    case "mainConent" => mainConent
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
    val limit = 18
    idx match {
      case "0" =>
        brands = Brand.findAll(MaxRows[Brand](limit), OrderBy(Brand.id, Descending))
      case "1" =>
        brands = Brand.findAll(StartAt(30), MaxRows[Brand](limit), OrderBy(Brand.id, Descending))
      case "2" =>
        brands = Brand.findAll(StartAt(0), MaxRows[Brand](limit), OrderBy(Brand.createdAt, Descending))
      case "3" =>
        brands = Brand.findAll(StartAt(0), MaxRows[Brand](limit), OrderBy(Brand.name, Descending))
    }
    "li" #> brands.map(brand => {
      ".brand-img *" #> <img src={ brand.displayPicSrc() } alt={ brand.name.get }/> &
        ".brand-name *" #> <a href={ "/market/view?id=" + brand.id.get } target="_blank">{ brand.name.get }</a> &
        ".price *" #> brand.displaySellPrice()

    })
  }

  def mainConent = {
    S.attr("brandTypeId") match {
      case Full(brandTypeId) => mainBrandDatas(brandTypeId.toInt)
      case _ => "*" #> Text("")
    }
  }

  def mainBrandDatas(brandTypeId: Int) = {
    val limit = S.attr("limit").map(_.toInt).openOr(24)
    val brands = Brand.findAll(By(Brand.brandTypeId, brandTypeId), MaxRows[Brand](limit))

    val brandType = BrandTypeHelper.brandTypes.get(brandTypeId)
    val tp = brandType match {
      case Some(t) => "#title" #> t.name
      case _ => "#title" #> Text("")
    }

    val dataList = ".brands li" #> brands.map(brand => {
      ".brand-img *" #> <img src={ brand.displayPicSrc() } alt={ brand.name.get }/> &
        ".brand-name *" #> <a href={ "/market/view?id=" + brand.id.get } target="_blank">{ brand.name.get }</a> &
        ".price *" #> brand.displaySellPrice()

    })

    tp & dataList
  }
}