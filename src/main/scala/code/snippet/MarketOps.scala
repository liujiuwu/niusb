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
    val (searchType, keyword, order) = (S.param("type"), S.param("keyword"), S.param("order"))
    val byBuffer = ArrayBuffer[QueryParam[Brand]]()
    searchType match {
      case Full(t) if (BrandType.isBrandType(t.toInt)) =>
        byBuffer += By(Brand.brandTypeCode, t.toInt)
      case _ =>
    }
    order match {
      case Full(orderBy) => orderBy match {
        case "price-z" => byBuffer += OrderBy(Brand.basePrice, Descending)
        case "price-a" => byBuffer += OrderBy(Brand.basePrice, Ascending)
        case "recommend" => byBuffer += By(Brand.recommend, true)
        case "hot" => byBuffer += OrderBy(Brand.concernCount, Descending)
      }
      case _ => byBuffer += OrderBy(Brand.id, Descending)
    }
    byBuffer.toList
  }

  def list = {
    val (searchType, keyword, order) = (S.param("type"), S.param("keyword"), S.param("order"))
    val limit = S.attr("limit").map(_.toInt).openOr(40)

    var url = originalUri
    var orderUrl = url
    var searchTypeVal, keywordVal, orderVal = ""
    searchType match {
      case Full(t) =>
        searchTypeVal = t
        url = appendParams(url, List("type" -> t))
        orderUrl = url
      case _ =>
    }
    keyword match {
      case Full(k) if (!k.trim().isEmpty()) =>
        keywordVal = k
        url = appendParams(url, List("keyword" -> k))
        orderUrl = url
      case _ =>
    }

    var orderBy, orderDirectional = ""
    order match {
      case Full(o) if (!o.trim().isEmpty()) =>
        orderVal = o
        orderUrl = url
        orderBy = orderVal
        val os = orderVal.split("-")
        if (os.length >= 2) {
          orderBy = os(0)
          orderDirectional = os(1)
        }
        url = appendParams(url, List("order" -> orderVal))
      case _ =>
    }

    val orderTools = ".lift-price [href]" #> appendParams(orderUrl, List("order" -> "price-z")) & ".lift-price-z [href]" #> appendParams(orderUrl, List("order" -> "price-z")) &
      ".lift-price-a [href]" #> appendParams(orderUrl, List("order" -> "price-a")) &
      ".lift-hot [href]" #> appendParams(orderUrl, List("order" -> "hot")) &
      ".lift-recommend [href]" #> appendParams(orderUrl, List("order" -> "recommend")) &
      s".lift-${orderBy} [class+]" #> "active" &
      s".lift-${orderBy} *" #> {
        orderDirectional match {
          case "a" => <lift:children><i class="icon-jpy"></i> 从低到高</lift:children>
          case "z" => <lift:children><i class="icon-jpy"></i> 从高到低</lift:children>
          case _ => <lift:children><i class="icon-jpy"></i> 价格</lift:children>
        }
      }

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