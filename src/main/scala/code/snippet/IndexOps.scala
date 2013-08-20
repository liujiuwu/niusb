package code.snippet

import scala.xml.Text
import code.lib.WebCacheHelper
import code.model.Brand
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import code.model.User
import code.lib.SmsHelper

object IndexOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "tabConent" => tabConent
    case "mainConent" => mainConent
  }

  def tabConent = {
    User.currentUser match {
      case Full(u) => println(u.mobile + "|" + SmsHelper.getSendSmsCode(u.mobile.get))
      case _ =>
    }

    S.attr("tabIdx") match {
      case Full(idx) => brandDatas(idx)
      case _ => "*" #> Text("")
    }
  }

  def brandDatas(idx: String) = {
    WebCacheHelper.indexTabBrands.get(idx) match {
      case Some(brands) => "li" #> brands.map(_.displayBrand)
      case _ => "*" #> "无数据"
    }
  }

  def mainConent = {
    S.attr("brandTypeCode") match {
      case Full(brandTypeCode) => mainBrandDatas(brandTypeCode.toInt)
      case _ => "*" #> Text("")
    }
  }

  def mainBrandDatas(brandTypeCode: Int) = {
    val limit = S.attr("limit").map(_.toInt).openOr(24)
    val dataList = WebCacheHelper.indexBrandsByType.get(brandTypeCode) match {
      case Some(brands) => ".brands li" #> brands.slice(0, limit).map(_.displayBrand)
      case _ => "*" #> "无数据"
    }

    val tp = "#title" #> (WebCacheHelper.brandTypes.get(brandTypeCode) match {
      case Some(b) => Text(b.name.get)
      case _ => Text("所有类型")
    })

    tp & dataList
  }
}