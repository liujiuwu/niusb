package code.snippet

import scala.xml.NodeSeq
import scala.xml.Text
import code.model.Brand
import code.model.User
import code.model.UserData
import com.niusb.util.WebHelpers._
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.mapper.By
import net.liftweb.util.CssSel
import net.liftweb.util.Helpers._
import com.niusb.util.SearchBrandFormHelpers

object MarketOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "list" => list
    case "view" => view
  }

  def list = {
    val limit = S.attr("limit").map(_.toInt).openOr(40)
    val formParam = SearchBrandFormHelpers.getSearchBrandFormParam()
    val searchForm = "#searchBrandForm" #> SearchBrandFormHelpers.form(formParam)
    val paginatorModel = Brand.paginator(formParam.url, SearchBrandFormHelpers.searchBrandFormBies(formParam): _*)(itemsOnPage = limit)
    val pagination = "#pagination" #> paginatorModel.paginate _

    val dataList = ".brands li" #> paginatorModel.datas.map(_.displayBrand)
    "#title" #> formParam.brandTypeName & searchForm & dataList & pagination
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