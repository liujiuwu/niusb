package code.snippet

import scala.xml.Text
import code.model.Brand
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.mapper._
import net.liftweb.util.Helpers._
import code.model.BrandType
import code.model.AdSpace

object IndexOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "brandTypes" => brandTypes
    case "tabConent" => tabConent
    case "mainConent" => mainConent
  }

  def brandTypes = {
    val brandTypes = BrandType.getBrandTypes()
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
      ".brand-img *" #> <a href={ "/market/view?id=" + brand.id.get } target="_blank"><img src={ brand.displayPicSrc() } alt={ brand.name.get }/></a> &
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

  def mainBrandDatas(brandTypeCode: Int) = {
    val limit = S.attr("limit").map(_.toInt).openOr(24)
    val brands = Brand.findAll(By(Brand.brandTypeCode, brandTypeCode), MaxRows[Brand](limit))

    val brandType = BrandType.getBrandTypes().get(brandTypeCode)
    val tp = brandType match {
      case Some(t) => "#title" #> t.name
      case _ => "#title" #> Text("")
    }

    val dataList = ".brands li" #> brands.map(brand => {
      ".brand-img *" #> <a href={ "/market/view?id=" + brand.id.get } target="_blank"><img src={ brand.displayPicSrc() } alt={ brand.name.get }/></a> &
        ".brand-name *" #> <a href={ "/market/view?id=" + brand.id.get } target="_blank">{ brand.name.get }</a> &
        ".price *" #> brand.displaySellPrice()

    })

    tp & dataList
  }
}