package code.snippet.admin

import scala.xml.NodeSeq
import scala.xml.Text
import code.lib.BrandType
import code.lib.BrandTypeHelper
import code.lib.SearchHelper
import code.lib.WebHelper
import code.model.Brand
import code.model.BrandStatus
import code.model.User
import code.snippet.MyPaginatorSnippet
import net.liftweb.common.Box
import net.liftweb.common.Box.box2Option
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.http.RequestVar
import net.liftweb.http.S
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.SHtml.ajaxCall
import net.liftweb.http.SHtml.hidden
import net.liftweb.http.SHtml.link
import net.liftweb.http.SHtml.select
import net.liftweb.http.SHtml.text
import net.liftweb.http.SHtml.textarea
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JE.ValById
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmds.SetValById
import net.liftweb.http.js.JsCmds.cmdToString
import net.liftweb.http.js.JsCmds.jsExpToJsCmd
import net.liftweb.http.js.JsExp.strToJsExp
import net.liftweb.mapper.By
import net.liftweb.mapper.Descending
import net.liftweb.mapper.MaxRows
import net.liftweb.mapper.OrderBy
import net.liftweb.mapper.StartAt
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import net.liftweb.util.Helpers.tryo
import code.snippet.TabMenu

object BrandOps extends TabMenu with MyPaginatorSnippet[Brand] {
  object brandRV extends RequestVar[Brand](Brand.create)
  override def itemsPerPage = 10
  override def count = Brand.count()
  override def page = Brand.findAll(StartAt(curPage * itemsPerPage), MaxRows(itemsPerPage), OrderBy(Brand.createdAt, Descending))

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