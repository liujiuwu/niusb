package code.snippet.admin

import scala.xml.NodeSeq
import scala.xml.Text

import code.lib.BrandTypeHelper
import code.lib.WebHelper
import code.model.Brand
import code.model.BrandStatus
import code.model.User
import code.snippet.MyPaginatorSnippet
import code.snippet.TabMenu
import net.liftweb.common.Box
import net.liftweb.common.Box.box2Option
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.http.RequestVar
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.SHtml.link
import net.liftweb.mapper.By
import net.liftweb.mapper.Descending
import net.liftweb.mapper.MaxRows
import net.liftweb.mapper.NotBy
import net.liftweb.mapper.OrderBy
import net.liftweb.mapper.StartAt
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.util.Helpers.strToSuperArrowAssoc

object BrandOps extends TabMenu with MyPaginatorSnippet[Brand] {
  object brandRV extends RequestVar[Brand](Brand.create)
  object userRV extends RequestVar[Box[User]](Empty)

  override def itemsPerPage = 10
  override def count = {
    userRV.is match {
      case Full(user) => Brand.count(By(Brand.owner, user))
      case _ => Brand.count()
    }
  }
  override def page = {
    println(userRV.is + "=================")
    userRV.is match {
      case Full(user) =>
        Brand.findAll(By(Brand.owner, user), StartAt(curPage * itemsPerPage), MaxRows(itemsPerPage), OrderBy(Brand.createdAt, Descending))
      case _ => Brand.findAll(StartAt(curPage * itemsPerPage), MaxRows(itemsPerPage), OrderBy(Brand.createdAt, Descending))
    }

  }

  def list = {
    def actions(brand: Brand): NodeSeq = {
      brand.status.get match {
        case _ =>
          link("/admin/brand/view",
            () => brandRV(brand), <i class="icon-zoom-in"></i>, "class" -> "btn btn-small btn-success") ++ Text(" ") ++
            link("/admin/brand/edit",
              () => brandRV(brand), <i class="icon-edit"></i>, "class" -> "btn btn-small btn-info") ++ Text(" ") ++
              link("/admin/brand/", () => { brand.delete_! }, <i class="icon-trash"></i>, "class" -> "btn btn-small btn-danger")
      }
    }

    "tr" #> page.map(brand => {
      val brandType = BrandTypeHelper.brandTypes.get(brand.brandTypeId.get).get
      "#regNo" #> brand.regNo.get &
        "#name" #> brand.name.get &
        "#brandType" #> { brandType.id + " -> " + brandType.name } &
        "#applicant" #> brand.applicant.get &
        "#regDate" #> brand.regDate.asHtml &
        "#status" #> WebHelper.statusLabel(brand.status.get) &
        "#basePrice" #> <span class="badge badge-success">￥{ brand.basePrice }</span> &
        "#sellPrice" #> <span class="badge badge-warning">￥{ brand.sellPrice }</span> &
        "#owner" #> brand.owner.getOwner.name &
        "#actions" #> actions(brand)
    })
  }

  def view = {
    tabMenuRV(Full("zoom-in", "查看商标"))
    val brand = brandRV.is
    val brandType = BrandTypeHelper.brandTypes.get(brand.brandTypeId.get).get

    "#regNo" #> brand.regNo &
      "#name" #> brand.name &
      "#brand-type" #> { brandType.id + " -> " + brandType.name } &
      "#status" #> WebHelper.statusLabel(brand.status.get) &
      "#basePrice" #> <span class="badge badge-success">￥{ brand.basePrice }</span> &
      "#sellPrice" #> <span class="badge badge-warning">￥{ brand.sellPrice }</span> &
      "#strikePrice" #> <span class="badge badge-important">￥{ brand.strikePrice }</span> &
      "#regdate" #> brand.regDate.asHtml &
      "#applicant" #> brand.applicant &
      "#useDescn" #> brand.useDescn &
      "#descn" #> brand.descn &
      "#owner" #> brand.owner.getOwner.name
  }

}