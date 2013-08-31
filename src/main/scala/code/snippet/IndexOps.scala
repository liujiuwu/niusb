package code.snippet

import scala.xml.Text
import code.lib.WebCacheHelper
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.util.Helpers.strToCssBindPromoter
import scala.xml.NodeSeq
import com.niusb.util.WebHelpers._
import com.niusb.util.SearchBrandFormHelpers
import code.model.Article
import net.liftweb.mapper.By
import code.model.ArticleType

object IndexOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "tabConent" => tabConent
    case "mainConent" => mainConent
    case "searchBrandForm" => searchBrandForm
    case "news" => news
  }

  def tabConent = {
    S.attr("tabIdx") match {
      case Full(idx) => brandDatas(idx)
      case _ => "*" #> Text("")
    }
  }

  private def brandDatas(idx: String) = {
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

  private def mainBrandDatas(brandTypeCode: Int) = {
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

  def searchBrandForm = {
    val formParam = SearchBrandFormHelpers.getSearchBrandFormParam()
    "#searchBrandForm" #> SearchBrandFormHelpers.form(formParam)
  }

  def news = {
    val limit = S.attr("limit").map(_.toInt).openOr(6)
    val bies = By(Article.articleType, ArticleType.News)
    val paginatorModel = Article.paginator(originalUri, bies)(itemsOnPage = limit)
    val dataList = "li *" #> paginatorModel.datas.map(_.title.displayTitle)
    dataList
  }
}