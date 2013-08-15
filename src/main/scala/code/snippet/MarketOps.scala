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
import net.liftweb.common._
import scala.collection.mutable.ArrayBuffer

object MarketOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "list" => list
    case "brandTypes" => brandTypes
    case "view" => view
  }

  private def bies: List[QueryParam[Brand]] = {
    val (searchType, keyword) = (S.param("type"), S.param("keyword"))
    val byBuffer = ArrayBuffer[QueryParam[Brand]](OrderBy(Brand.id, Descending))
    searchType match {
      case Full(t) if (BrandType.isBrandType(t.toInt)) =>
        byBuffer += By(Brand.brandTypeCode, t.toInt)
      case _ =>
    }
    byBuffer.toList
  }

  def list = {
    val (searchType, keyword, order) = (S.param("type"), S.param("keyword"), S.param("order"))
    val limit = S.attr("limit").map(_.toInt).openOr(40)

    var url = originalUri
    var searchTypeVal, keywordVal, orderVal = ""
    searchType match {
      case Full(t) =>
        searchTypeVal = t
        url = appendParams(url, List("type" -> t))
      case _ =>
    }
    keyword match {
      case Full(k) if (!k.trim().isEmpty()) =>
        keywordVal = k
        url = appendParams(url, List("keyword" -> k))
      case _ =>
    }
    order match {
      case Full(o) if (!o.trim().isEmpty()) =>
        orderVal = o
        url = appendParams(url, List("order" -> orderVal))
      case _ =>
    }

    val orderTools = ".lift-price [href]" #> appendParams(url, List("order" -> "price")) &
      ".lift-hot [href]" #> appendParams(url, List("order" -> "hot")) &
      ".lift-recommend [href]" #> appendParams(url, List("order" -> "recommend")) &
      s".lift-${orderVal} [class+]" #> "active"

    val paginatorModel = Brand.paginator(url, bies: _*)(itemsOnPage = limit)
    val dataList = ".brands li" #> paginatorModel.datas.map(brand => {
      ".brand-img *" #> <a href={ "/market/view?id=" + brand.id.get } target="_blank"><img src={ brand.pic.src } alt={ brand.name.get.trim }/></a> &
        ".brand-name *" #> <a href={ "/market/view?id=" + brand.id.get } target="_blank">{ brand.name.get.trim }</a> &
        ".price *" #> brand.sellPrice.displaySellPrice()
    })

    orderTools & dataList & "#pagination" #> paginatorModel.paginate _
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
      "li *" #> <a href={ "/market/index?type=" + b.code }>{ b.displayTypeName() }</a>
    })
  }

}