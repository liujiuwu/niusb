package code.snippet

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.LinkedHashMap
import scala.util.Success
import scala.util.Try
import scala.xml.NodeSeq
import scala.xml.Text
import com.niusb.util.MemHelpers
import com.niusb.util.SearchBrandFormHelpers
import code.lib.WebCacheHelper
import code.model.Brand
import code.model.BrandStatus
import code.model.User
import code.model.UserData
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.util.Helpers.asLong
import net.liftweb.util.Helpers.intToTimeSpanBuilder
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import net.liftweb.http.js.JE.ValById
import net.liftweb.common.Full

object MarketOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "list" => list
    case "view" => view
    case "brandTypes" => brandTypes
    case "brandNavBox" => brandNavBox
    case "searchBrand" => searchBrand
  }

  val marketMenus = LinkedHashMap[String, NodeSeq](
    "/market/0/0/0" -> Text("全部商标"),
    "/market/1/0/0" -> Text("精品商标"),
    "/market/2/0/0" -> Text("特价商标"),
    "/market/3/0/0" -> Text("自有商标"))

  def getSearchKeyword() = {
    S.param("keyword").openOr("")
  }

  def list = {
    val limit = S.attr("limit").map(_.toInt).openOr(40)
    val pageType = Try(S.param("pageType").openOr("0").toInt) match {
      case Success(code) => code
      case _ => 0
    }

    val brandTypeCode = Try(S.param("brandTypeCode").openOr("0").toInt) match {
      case Success(code) => code
      case _ => 0
    }

    val brandTypeName = WebCacheHelper.brandTypes.get(brandTypeCode) match {
      case Some(brandType) => brandType.name.is
      case _ => "所有商标类型"
    }

    val orderType = Try(S.param("orderType").openOr("0").toInt) match {
      case Success(code) => code
      case _ => 0
    }

    val byBuffer = ArrayBuffer[QueryParam[Brand]](By(Brand.status, BrandStatus.ChuShoZhong))
    val keyword = getSearchKeyword()
    if (!keyword.trim().isEmpty()) {
      byBuffer += Like(Brand.name, s"%${keyword}%")
    }

    if (pageType > 0) {
      pageType match {
        case 1 => byBuffer += By(Brand.isRecommend, true)
        case 2 => byBuffer += By(Brand.isOffer, true)
        case 3 => byBuffer += By(Brand.isOwn, true)
      }
    }

    if (brandTypeCode > 0) {
      byBuffer += By(Brand.brandTypeCode, brandTypeCode)
    }

    orderType match {
      case 0 => byBuffer += OrderBy(Brand.id, Descending)
      case 1 =>
        byBuffer += By_>(Brand.basePrice, 0)
        byBuffer += OrderBy(Brand.basePrice, Ascending)
      case 2 =>
        byBuffer += By_>(Brand.basePrice, 0)
        byBuffer += OrderBy(Brand.basePrice, Descending)
      case 3 => byBuffer += OrderBy(Brand.viewCount, Descending)
      case 4 => byBuffer += OrderBy(Brand.followCount, Descending)
      case _ => byBuffer += OrderBy(Brand.id, Descending)
    }

    val defaultTab = for (menu <- marketMenus) yield {
      val cls = if (menu._1.startsWith("/market/" + pageType)) "active" else null
      <li class={ cls }><a href={ Helpers.appendParams(menu._1, List("keyword" -> keyword)) }>{ menu._2 }</a></li>
    }

    val marketNav = <ul class="nav nav-tabs">{ defaultTab }</ul>

    val pageUrl = Helpers.appendParams(Brand.pageUrl(pageType, brandTypeCode, orderType), List("keyword" -> keyword))
    val paginatorModel = Brand.paginator(pageUrl, byBuffer.toList: _*)(itemsOnPage = limit)
    val pagination = "#pagination" #> paginatorModel.paginate _

    val dataList = ".brands li" #> paginatorModel.datas.map(_.displayBrand)
    val orderTypeList = "#order-type-list dd" #> SearchBrandFormHelpers.orderTypes.map { ot =>
      "a *" #> ot._2 &
        "a [href]" #> { Brand.pageUrl(pageType, brandTypeCode, ot._1.toInt) } &
        (if (ot._1.toInt == orderType) "a [class+]" #> "active" else "a [class!]" #> "active")
    }

    val pageTitle = if (!keyword.isEmpty()) {
      s"""与"${keyword}"相关的${brandTypeName}商标"""
    } else {
      brandTypeName
    }
    "#title" #> pageTitle & "#marketNav" #> marketNav & orderTypeList & dataList & pagination
  }

  def view = {
    def followBtn(brand: Brand): NodeSeq = {
      def cancelFollowBtn = a(() => requiredLogin(follow), Text("取消关注"), "class" -> "btn btn-danger btn-xs")
      def followBtn = a(() => requiredLogin(follow), Text("关注此商标"), "class" -> "btn btn-primary btn-xs")
      def userData = UserData.getOrCreateUserData(loginUser.id.get)
      def isFollow = userData.isFollow(brand.id.get)

      def follow(): JsCmd = {
        if (isFollow) {
          val followCount = brand.followCount.decr
          userData.cancelFollow(brand.id.get)
          SetHtml("followCount", Text(followCount.toString)) & SetHtml("followCountBtn", followBtn)
        } else {
          val followCount = brand.followCount.incr
          userData.prependFollow(brand.id.get)
          SetHtml("followCount", Text(followCount.toString)) & SetHtml("followCountBtn", cancelFollowBtn)
        }
      }
      if (User.loggedIn_?) {
        if (isFollow) cancelFollowBtn else followBtn
      } else {
        followBtn
      }
    }

    (for {
      brandId <- S.param("id").flatMap(asLong) ?~ "商标ID不存在或无效"
      brand <- Brand.find(By(Brand.id, brandId)) ?~ s"ID为${brandId}的商标不存在。"
    } yield {
      val brandAdList = brand.adPic.adPics match {
        case Full(ads) =>
          "li *" #> ads.map { ad =>
            "img [src]" #> (s"/upload/ads/${brand.id.is}/" + ad)
          }
        case _ => ClearNodes
      }
      "#title" #> brand.name &
        "#brand-img" #> <img src={ brand.pic.src } alt={ brand.name.get.trim } width="320" height="200"/> &
        "#brandId" #> Text(brand.brandTypeCode + "-" + brand.id) &
        "#regNo" #> brand.regNo &
        "#sellPrice" #> brand.sellPrice.displaySellPrice() &
        "#regdate" #> brand.regDate.asHtml &
        "#lsqz" #> brand.lsqz &
        "#useDescn" #> brand.useDescn &
        "#viewCount *" #> brand.viewCount.incr(realIp) &
        "#followCount *" #> brand.followCount.get &
        "#followCountBtn *" #> followBtn(brand) &
        "#descn" #> brand.descn.display &
        "#brand-ad-list" #> brandAdList
    }): CssSel
  }

  def brandTypes = {
    "li *" #> WebCacheHelper.brandTypes.values.map(_.name.displayTypeName())
  }

  def brandNavBox = {
    val keyword = getSearchKeyword()
    val allBrandType = "#allBrandType" #> <a href={ "/market?keyword=" + keyword }>所有商标类型</a>
    val brandTypeList = "li *" #> WebCacheHelper.brandTypes.values.map(_.name.displayTypeName(keyword, false))
    brandTypeList & allBrandType
  }

  def searchBrand = {
    val keyword = getSearchKeyword()
    "#search-btn [onclick]" #> ajaxCall(ValById("keyword"), keyword => {
      S.redirectTo("/market?keyword=" + keyword)
    })

  }
}