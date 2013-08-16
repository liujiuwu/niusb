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
    val (brandTypeCode, keywordType, keyword, likeType, orderType) = (S.param("btc"), S.param("kt"), S.param("k"), S.param("lt"), S.param("ot"))
    val byBuffer = ArrayBuffer[QueryParam[Brand]]()
    brandTypeCode match {
      case Full(code) if (code != "all" && BrandType.isBrandType(code.toInt)) =>
        byBuffer += By(Brand.brandTypeCode, code.toInt)
      case _ =>
    }

    keyword match {
      case Full(k) if (!k.trim().isEmpty()) => {
        val kv = k.trim
        val field = keywordType match {
          case Full(kt) => kt match {
            case "0" => Full(Brand.name)
            case "1" => Full(Brand.regNo)
            case _ => Full(Brand.name)
          }
          case _ => Full(Brand.name)

        }
        likeType match {
          case Full(like) => like match {
            case "0" => byBuffer += By(field.get, kv)
            case "1" => byBuffer += Like(field.get, "%" + kv + "%")
            case "2" => byBuffer += Like(field.get, kv + "%")
            case "3" => byBuffer += Like(field.get, "%" + kv)
            case _ =>
          }
          case _ =>
        }
      }
      case _ =>
    }

    orderType match {
      case Full(order) => order match {
        case "0" =>
          byBuffer += OrderBy(Brand.id, Descending)
        case "1" =>
          byBuffer += OrderBy(Brand.basePrice, Ascending)
        case "2" =>
          byBuffer += OrderBy(Brand.basePrice, Descending)
        case "3" =>
          byBuffer += By(Brand.recommend, true)
        case "4" =>
          byBuffer += OrderBy(Brand.concernCount, Descending)
        case _ =>
      }

      case _ =>
    }

    byBuffer.toList
  }

  def list = {
    val (brandTypeCode, keywordType, keyword, likeType, orderType) = (S.param("btc"), S.param("kt"), S.param("k"), S.param("lt"), S.param("ot"))
    val limit = S.attr("limit").map(_.toInt).openOr(40)

    var url = originalUri
    var brandTypeCodeVal, keywordTypeVal, keywordVal, likeTypeVal, orderVal = ""
    brandTypeCode match {
      case Full(code) =>
        brandTypeCodeVal = code
        url = appendParams(url, List("btc" -> code))
      case _ =>
    }

    keywordType match {
      case Full(t) =>
        keywordTypeVal = t
        url = appendParams(url, List("kt" -> t))
      case _ =>
    }

    keyword match {
      case Full(k) if (!k.trim().isEmpty()) =>
        keywordVal = k.trim
        url = appendParams(url, List("k" -> keywordVal))
      case _ =>
    }

    likeType match {
      case Full(like) =>
        likeTypeVal = like
        url = appendParams(url, List("lt" -> like))
      case _ =>
    }

    orderType match {
      case Full(order) =>
        orderVal = order
        url = appendParams(url, List("ot" -> order))
      case _ =>
    }

    val searchForm = "#searchForm" #>
      <form class="form-inline" action={ url } method="get">
        <div class="controls">
          <select id="btc" name="btc">
            <option value="all">所有商标类型</option>
            { SelectBoxHelper.brandTypeOptions(brandTypeCodeVal) }
          </select>
          <select id="kt" name="kt" class="span3">
            { SelectBoxHelper.keywordTypeOptions(keywordTypeVal) }
          </select>
          <input type="text" id="k" name="k" placeholder="搜索关键词" value={ keywordVal } class="span7"/>
          <select id="lt" name="lt" class="span3">
            { SelectBoxHelper.likeOptions(likeTypeVal) }
          </select>
          <select id="ot" name="ot" class="span4">
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