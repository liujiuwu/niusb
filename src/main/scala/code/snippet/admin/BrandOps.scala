package code.snippet.admin

import scala.xml.NodeSeq
import scala.xml.Text
import code.lib.BrandTypeHelper
import code.lib.WebHelper
import code.model.Brand
import code.model.User
import net.liftweb.common.Box
import net.liftweb.common.Box.box2Option
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.RequestVar
import net.liftweb.http.SHtml._
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.mapper.By
import net.liftweb.mapper.BySql
import net.liftweb.mapper.Cmp
import net.liftweb.mapper.Descending
import net.liftweb.mapper.IHaveValidatedThisSQL
import net.liftweb.mapper.MaxRows
import net.liftweb.mapper.OrderBy
import net.liftweb.mapper.QueryParam
import net.liftweb.mapper.StartAt
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import net.liftweb.http.S
import code.lib.BrandType
import net.liftweb.http.js.JsCmd
import net.liftweb.common.Empty
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd._
import net.liftweb.http.js.JsCmds._
import net.liftweb.util.Helpers._
import code.model.BrandStatus
import net.coobird.thumbnailator.Thumbnails
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import code.rest.UploadManager
import java.io.File
import net.liftweb.util.CssSel
import code.snippet.PaginatorHelper
import code.snippet.SnippetHelper

object BrandOps extends SnippetHelper with Loggable {
  private object typeRV extends RequestVar[Box[String]](Full("0"))
  private object keywordRV extends RequestVar[String]("")
  //object brandRV extends RequestVar[Brand](Brand.create)

  val itemsPerPage = 2

  private def queryParam = {
    val keyword = keywordRV.is
    val searchType = typeRV.is.openOrThrowException("no select search type")
    logger.info("keyword=" + keyword + ",searchType=" + searchType)
    if (keyword != "") {
      if (searchType == "0") {
        By(Brand.regNo, keyword)
      } else if (searchType == "1") {
        BySql("owner=" + keyword, IHaveValidatedThisSQL("charliechen", "2011-07-21"))
      }
    } else {

    }
  }

  /*override def count = {
    Brand.count()

  }*/

  /* override def page = {
    Brand.findAll(StartAt(curPage * itemsPerPage), MaxRows(itemsPerPage), OrderBy(Brand.createdAt, Descending))
  }*/

  def list = {
    val page: Long = S.param("page").map(toLong) openOr 1
    val q = S.param("type").map(toInt) openOr 1
    val paginatorModel = Brand.paginator(page, itemsPerPage, By(Brand.brandTypeId, q), OrderBy(Brand.createdAt, Descending))
    def actions(brand: Brand): NodeSeq = {
      brand.status.get match {
        case _ =>
          <a href={ "/admin/brand/edit?id=" + brand.id.get } class="btn btn-small btn-info"><i class="icon-edit"></i></a> ++ Text(" ") ++
            link("/admin/brand/", () => { brand.delete_! }, <i class="icon-trash"></i>, "class" -> "btn btn-small btn-danger")
      }
    }

    val dataList = "#dataList tr" #> paginatorModel.datas.map(brand => {
      "#regNo" #> brand.regNo.get &
        "#name" #> <a href={ "/admin/brand/view?id=" + brand.id.get }>{ brand.name }</a> &
        "#brandType" #> brand.displayType &
        "#regDate" #> brand.regDate.asHtml &
        "#status" #> brand.displayStatus &
        "#basePrice" #> brand.displayBasePrice &
        "#sellPrice" #> brand.displaySellPrice &
        "#strikePrice" #> brand.displayStrikePrice &
        "#owner" #> brand.owner.getOwner.name &
        "#actions" #> actions(brand)
    })

    val paginator = "#pagination" #> paginatorModel.paginate _

    paginator & dataList
  }

  def view = {
    tabMenuRV(Full("zoom-in" -> "查看商标"))

    (for {
      brandId <- S.param("id").flatMap(asLong) ?~ "商标ID不存在或无效"
      brand <- Brand.find(By(Brand.id, brandId)) ?~ s"ID为${brandId}的商标不存在。"
    } yield {
      "#regNo" #> brand.regNo &
        "#name" #> brand.name &
        "#brand-type" #> brand.displayType &
        "#status" #> brand.displayStatus &
        "#basePrice" #> brand.displayBasePrice &
        "#sellPrice" #> brand.displaySellPrice &
        "#strikePrice" #> brand.displayStrikePrice &
        "#regdate" #> brand.regDate.asHtml &
        "#applicant" #> brand.applicant &
        "#useDescn" #> brand.useDescn &
        "#descn" #> brand.descn &
        "#pic" #> brand.displayPic() &
        "#spic" #> brand.displaySpic &
        "#owner" #> brand.owner.getOwner.displayInfo &
        "#edit-btn" #> <a href={ "/admin/brand/edit?id=" + brand.id.get } class="btn btn-primary"><i class="icon-edit"></i> 修改商标</a> &
        "#list-btn" #> <a href="/admin/brand/" class="btn btn-success"><i class="icon-list"></i> 商标列表</a>
    }): CssSel
  }

  def search = {
    val types = List("0" -> "注册号", "1" -> "用户ID")
    "@type" #> select(types, typeRV.is, x => typeRV(Full(x))) &
      "@keyword" #> textElem(keywordRV)
  }

  def edit = {
    tabMenuRV(Full("edit" -> "修改商标"))

    (for {
      brandId <- S.param("id").flatMap(asLong) ?~ "商标ID不存在或无效"
      brand <- Brand.find(By(Brand.id, brandId)) ?~ s"ID为${brandId}的商标不存在。"
    } yield {
      var basePrice = "0"
      var regNo, pic, name, regDateStr, applicant, useDescn, descn = ""
      var brandType: BrandType = BrandTypeHelper.brandTypes.get(brand.brandTypeId.get).get

      def process(): JsCmd = {
        val oldPic = brand.pic.get
        brand.regNo(regNo).basePrice(basePrice.toInt).pic(pic).name(name).regDate(WebHelper.dateParse(regDateStr).openOrThrowException("商标注册日期错误")).applicant(applicant).useDescn(useDescn).descn(descn)
        brand.brandTypeId(brandType.id)
        brand.validate match {
          case Nil =>
            brand.save
            UploadManager.handleBrandImg(pic)
            if (oldPic != pic) {
              val oldPicFilex320 = new File(UploadManager.uploadBrandDir + File.separator + UploadManager.sizePicName(oldPic))
              if (oldPicFilex320.exists()) {
                FileUtils.deleteQuietly(oldPicFilex320)
              }
              val oldPicFilex128 = new File(UploadManager.uploadBrandDir + File.separator + UploadManager.sizePicName(oldPic, "128"))
              if (oldPicFilex128.exists()) {
                FileUtils.deleteQuietly(oldPicFilex128)
              }
            }
            //JsRaw(WebHelper.succMsg("opt_brand_tip", Text("商标信息已成功修改！")))
            S.redirectTo("/admin/brand/")
          case errors => println(errors); Noop
        }
      }

      val brandTypes = BrandTypeHelper.brandTypes.values.toList
      "@regNo" #> text(brand.regNo.get, regNo = _) &
        "@basePrice" #> text(brand.basePrice.get.toString, basePrice = _) &
        "@name" #> text(brand.name.get, name = _) &
        "@pic" #> hidden(pic = _, brand.pic.get) &
        "#brand_pic [src]" #> brand.displayPicSrc() &
        "@brand_status" #> selectObj[BrandStatus.Value](BrandStatus.values.toList.map(v => (v, v.toString)), Full(brand.status.is), brand.status(_)) &
        "@brand_type" #> select(brandTypes.map(v => (v.id.toString, v.id + " -> " + v.name)), Full(brandType.id.toString), v => (brandType = BrandTypeHelper.brandTypes.get(v.toInt).get)) &
        "@regDate" #> text(brand.regDate.asHtml.text, regDateStr = _) &
        "@applicant" #> text(brand.applicant.get, applicant = _) &
        "@useDescn" #> textarea(brand.useDescn.get, useDescn = _) &
        "@descn" #> textarea(brand.descn.get, descn = _) &
        "@sub" #> hidden(process)
    }): CssSel
  }

}