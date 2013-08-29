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
import code.lib.WebCacheHelper
import code.model.User
import scala.xml.NodeSeq
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmd._
import code.model.UserData
import code.lib.BootBoxHelper
import code.lib.BoxAlert
import net.liftweb.http.js.JE._
import code.lib.WebHelper

object MarketOps extends DispatchSnippet with SnippetHelper with SearchBrandForm with Loggable {
  def dispatch = {
    case "list" => list
    case "view" => view
  }

  def list = {
    val limit = S.attr("limit").map(_.toInt).openOr(40)
    val searchBrandFormParam = getSearchBrandFormParam()
    val searchForm = searchBrandForm(searchBrandFormParam)
    val paginatorModel = Brand.paginator(searchBrandFormParam.url, SearchBrandFormBies(searchBrandFormParam): _*)(itemsOnPage = limit)
    val dataList = ".brands li" #> paginatorModel.datas.map(_.displayBrand)
    "#title" #> searchBrandFormParam.brandTypeName & searchForm & dataList & "#pagination" #> paginatorModel.paginate _
  }

  def view = {
    def followBtn(brand: Brand) = {
      def userData = UserData.getOrCreateUserData(loginUser.id.get)
      def isFollow = userData.isFollow(brand.id.get)
      def cancelFollowBtn = SHtml.a(() => follow(), Text("取消关注"), "class" -> "btn btn-small btn-danger")
      def followBtn = SHtml.a(() => follow(), Text("关注此商标"), "class" -> "btn btn-small btn-primary")
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
      if (isFollow) cancelFollowBtn else followBtn
    }

    def followCountBtn(brand: Brand): NodeSeq = {
      if (User.loggedIn_?) {
        followBtn(brand)
      } else {
        <span><a class="btn btn-small btn-success" data-toggle="modal" data-target="#loginDialog">注册登录</a> 后可以关注此商标。</span>
      }
    }

    (for {
      brandId <- S.param("id").flatMap(asLong) ?~ "商标ID不存在或无效"
      brand <- Brand.find(By(Brand.id, brandId)) ?~ s"ID为${brandId}的商标不存在。"
    } yield {
      "#title" #> brand.name &
        "img" #> <img src={ brand.pic.src } alt={ brand.name.get.trim } width="320" height="200"/> &
        "#brandId" #> Text(brand.brandTypeCode + "-" + brand.id) &
        "#regNo" #> brand.regNo &
        "#sellPrice" #> brand.sellPrice.displaySellPrice() &
        "#regdate" #> brand.regDate.asHtml &
        "#lsqz" #> brand.lsqz &
        "#useDescn" #> brand.useDescn &
        "#viewCount *" #> brand.viewCount.incr(realIp) &
        "#followCount *" #> brand.followCount.get &
        "#followCountBtn *" #> followCountBtn(brand) &
        "#descn" #> brand.descn

    }): CssSel
  }
}