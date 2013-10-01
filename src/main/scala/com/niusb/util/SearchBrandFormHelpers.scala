package com.niusb.util

import code.lib.WebCacheHelper
import com.niusb.util.WebHelpers._
import net.liftweb.common.Full
import net.liftweb.http.S
import scala.xml.NodeSeq
import scala.util.Try
import scala.util.Success
import net.liftweb.util.Helpers._
import net.liftweb.mapper.QueryParam
import code.model.Brand
import scala.collection.mutable.ArrayBuffer
import net.liftweb.mapper._
import scala.xml.NodeSeq.seqToNodeSeq

object SearchBrandFormHelpers extends SearchBrandFormHelpers {
}

trait SearchBrandFormHelpers {
  case class SearchBrandFormParam(url: String, brandTypeName: String, brandTypeCode: String, keywordType: String, keyword: String, likeType: String, orderType: String, module: String)
  lazy val orderTypes = List[(String, String)]("0" -> "由新至旧", "1" -> "价格从低至高", "2" -> "价格从高至低", "3" -> "浏览次数", "4" -> "热门关注")
  lazy val likeTypes = List[(String, String)]("0" -> "精确", "1" -> "模糊", "2" -> "前包含", "3" -> "后包含")
  lazy val keywordTypes = List[(String, String)]("0" -> "商标名称", "1" -> "商标注册号")
  lazy val adminKeywordTypes = List[(String, String)]("0" -> "商标名称", "1" -> "商标注册号", "2" -> "商标ID", "3" -> "用户ID")
  lazy val brandTypes = WebCacheHelper.brandTypes.values.toList

  lazy val adminBrderTypes = orderTypes ::: List("5" -> "推荐", "6" -> "自有", "7" -> "特价")

  def orderOptions(selected: String): NodeSeq = {
    for (option <- orderTypes; (value, label) = option) yield {
      options(value, label, selected)
    }
  }

  def likeOptions(selected: String): NodeSeq = {
    for (option <- likeTypes; (value, label) = option) yield {
      options(value, label, selected)
    }
  }

  def keywordTypeOptions(selected: String): NodeSeq = {
    for (option <- keywordTypes; (value, label) = option) yield {
      options(value, label, selected)
    }
  }

  def brandTypeOptions(selected: String): NodeSeq = {
    <option value="all">所有商标类型</option> :: (for (option <- brandTypes; (value, label) = (option.code.get.toString, option.name.get)) yield {
      options(value, label, selected, true)
    })
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

    var url = S.attr("url").openOr(S.originalRequest.map(_.uri).openOr(sys.error("No request")))
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

    val module = S.attr("module").openOr("")
    SearchBrandFormParam(url, brandTypeName, brandTypeCodeVal, keywordTypeVal, keywordVal, likeTypeVal, orderVal, module)
  }

  def searchBrandFormBies(param: SearchBrandFormParam): List[QueryParam[Brand]] = {
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

    param.orderType match {
      case "0" =>
        byBuffer += OrderBy(Brand.id, Descending)
      case "1" =>
        byBuffer += OrderBy(Brand.basePrice, Ascending)
      case "2" =>
        byBuffer += OrderBy(Brand.basePrice, Descending)
      case "3" =>
        byBuffer += OrderBy(Brand.viewCount, Descending)
      case "4" =>
        byBuffer += OrderBy(Brand.followCount, Descending)
      case _ => byBuffer += OrderBy(Brand.id, Descending)
    }

    param.module match {
      case "recommend" => byBuffer += By(Brand.isRecommend, true)
      case "offer" => byBuffer += By(Brand.isOffer, true)
      case "own" => byBuffer += By(Brand.isOwn, true)
      case _ =>
    }

    byBuffer.toList
  }

  def form(formParam: SearchBrandFormParam) = {
    "form [action]" #> formParam.url &
      "#btc *" #> { brandTypeOptions(formParam.brandTypeCode) } &
      "#kt *" #> { keywordTypeOptions(formParam.keywordType) } &
      "#lt *" #> { likeOptions(formParam.likeType) } &
      "#ot *" #> { orderOptions(formParam.orderType) } &
      "@name [value]" #> formParam.keyword

  }
}