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
import code.lib.SelectBoxHelper

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
    val (brandTypeCode, keyword, likeType, orderType) = (S.param("brandTypeCode"), S.param("keyword"), S.param("likeType"), S.param("orderType"))
    val limit = S.attr("limit").map(_.toInt).openOr(40)

    var url = originalUri
    var brandTypeCodeVal, keywordVal, likeTypeVal, orderVal = ""
    brandTypeCode match {
      case Full(code) =>
        brandTypeCodeVal = code
        url = appendParams(url, List("brandTypeCode" -> code))
      case _ =>
    }

    keyword match {
      case Full(k) if (!k.trim().isEmpty()) =>
        keywordVal = k
        url = appendParams(url, List("keyword" -> k))
      case _ =>
    }

    likeType match {
      case Full(like) =>
        likeTypeVal = like
        url = appendParams(url, List("likeType" -> like))
      case _ =>
    }

    orderType match {
      case Full(order) =>
        orderVal = order
        url = appendParams(url, List("orderType" -> order))
      case _ =>
    }

    val searchForm = "#searchForm" #>
      <form class="form-inline" action={ url } method="get">
        <div class="controls">
          <select id="brandTypeCode" name="brandTypeCode">
            <option value="all">所有商标类型</option>
            { SelectBoxHelper.brandTypeOptions(brandTypeCodeVal) }
          </select>
          <input type="text" id="keyword" placeholder="商标名或注册号"/>
          <select id="likeType" name="likeType" class="span3">
            { SelectBoxHelper.likeOptions(likeTypeVal) }
          </select>
          <select id="orderType" name="orderType" class="span4">
            { SelectBoxHelper.orderOptions(orderVal) }
          </select>
          <button type="submit" class="btn">
            <i class="icon-search"></i>
            搜索
          </button>
        </div>
      </form>

    val paginatorModel = Brand.paginator(url, bies: _*)(itemsOnPage = limit)
    val dataList = ".brands li" #> paginatorModel.datas.map(brand => {
      ".brand-img *" #> <a href={ "/market/view?id=" + brand.id.get } target="_blank"><img src={ brand.pic.src } alt={ brand.name.get.trim }/></a> &
        ".brand-name *" #> <a href={ "/market/view?id=" + brand.id.get } target="_blank">{ brand.name.get.trim }</a> &
        ".price *" #> brand.sellPrice.displaySellPrice()
    })

    searchForm & dataList & "#pagination" #> paginatorModel.paginate _
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