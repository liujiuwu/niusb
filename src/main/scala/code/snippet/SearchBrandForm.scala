package code.snippet

import net.liftweb.http.S
import code.lib.WebCacheHelper
import scala.util.Try
import scala.util.Success
import net.liftweb.util.Helpers._
import code.lib.SelectBoxHelper
import net.liftweb.common.Full
import net.liftweb.mapper.QueryParam
import scala.collection.mutable.ArrayBuffer
import code.model.BrandType
import code.model.Brand
import net.liftweb.mapper._

trait SearchBrandForm {
  case class SearchBrandFormParam(url: String, brandTypeName: String, brandTypeCode: String, keywordType: String, keyword: String, likeType: String, order: String)
  def searchBrandForm(param: SearchBrandFormParam) = {
    val searchForm = {
      "#searchForm" #>
        <form class="form-inline" action={ param.url } method="get">
          <div class="controls">
            <select id="btc" name="btc">
              <option value="all">所有商标类型</option>
              { SelectBoxHelper.brandTypeOptions(param.brandTypeCode) }
            </select>
            <select id="kt" name="kt" class="span3">
              { SelectBoxHelper.keywordTypeOptions(param.keywordType) }
            </select>
            <input type="text" id="k" name="k" placeholder="搜索关键词" value={ param.keyword } class="span7"/>
            <select id="lt" name="lt" class="span3">
              { SelectBoxHelper.likeOptions(param.likeType) }
            </select>
            <select id="ot" name="ot" class="span4">
              { SelectBoxHelper.orderOptions(param.order) }
            </select>
            <button type="submit" class="btn">
              <i class="icon-search"></i>
              搜索
            </button>
          </div>
        </form>
    }

    searchForm
  }

  def getSearchBrandFormParam(): SearchBrandFormParam = {
    def getBrandTypeNameBycode(code: String): String = {
      Try(code.toInt) match {
        case Success(c) =>
          WebCacheHelper.brandTypes.get(c) match {
            case Some(b) => b.name.get
            case _ => "所有类型"
          }
        case _ => "所有类型"
      }
    }

    var url = S.originalRequest.map(_.uri).openOr(sys.error("No request"))
    val (brandTypeCode, keywordType, keyword, likeType, orderType) = (S.param("btc"), S.param("kt"), S.param("k"), S.param("lt"), S.param("ot"))

    val brandTypeCodeVal = brandTypeCode.openOr(S.attr("brandTypeCode").openOr("all"))
    val brandTypeName = getBrandTypeNameBycode(brandTypeCodeVal)
    if (brandTypeCodeVal != "all") {
      url = appendParams(url, List("btc" -> brandTypeCodeVal))
    }

    val keywordTypeVal = keywordType.openOr("")
    if (keywordTypeVal != "") {
      url = appendParams(url, List("kt" -> keywordTypeVal))
    }

    val keywordVal = keyword.openOr("")
    if (keywordVal != "") {
      url = appendParams(url, List("k" -> keywordVal))
    }

    val likeTypeVal = likeType.openOr("")
    if (likeTypeVal != "") {
      url = appendParams(url, List("lt" -> likeTypeVal))
    }

    val orderVal = orderType.openOr("")
    if (orderVal != "") {
      url = appendParams(url, List("ot" -> orderVal))
    }
    SearchBrandFormParam(url, brandTypeName, brandTypeCodeVal, keywordTypeVal, keywordVal, likeTypeVal, orderVal)
  }

  def SearchBrandFormBies(param: SearchBrandFormParam): List[QueryParam[Brand]] = {
    val byBuffer = ArrayBuffer[QueryParam[Brand]]()
    Try(param.brandTypeCode.toInt) match {
      case Success(code) => byBuffer += By(Brand.brandTypeCode, code)
      case _ =>
    }

    param.keyword match {
      case k if (!k.trim().isEmpty()) => {
        val kv = k.trim
        val field = param.keywordType match {
          case "0" => Full(Brand.name)
          case "1" => Full(Brand.regNo)
          case _ => Full(Brand.name)
        }
        param.likeType match {
          case "0" => byBuffer += By(field.get, kv)
          case "1" => byBuffer += Like(field.get, "%" + kv + "%")
          case "2" => byBuffer += Like(field.get, kv + "%")
          case "3" => byBuffer += Like(field.get, "%" + kv)
          case _ =>
        }
      }
      case _ =>
    }

    param.order match {
      case "0" =>
        byBuffer += OrderBy(Brand.id, Descending)
      case "1" =>
        byBuffer += OrderBy(Brand.basePrice, Ascending)
      case "2" =>
        byBuffer += OrderBy(Brand.basePrice, Descending)
      case "3" =>
        byBuffer += By(Brand.isRecommend, true)
      case "4" =>
        byBuffer += OrderBy(Brand.followCount, Descending)
      case _ => byBuffer += OrderBy(Brand.id, Descending)
    }

    byBuffer.toList
  }
}