package code.snippet.admin

import java.io.File
import scala.xml.NodeSeq
import scala.xml.Text
import org.apache.commons.io.FileUtils
import code.lib.BrandType
import code.lib.BrandTypeHelper
import code.model.Brand
import code.model.BrandStatus
import code.rest.UploadManager
import code.snippet.SnippetHelper
import net.liftweb.common._
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import code.lib.WebHelper

object BrandOps extends SnippetHelper with Loggable {

  def list = {
    def actions(brand: Brand): NodeSeq = {
      brand.status.get match {
        case _ =>
          <a href={ "/admin/brand/edit?id=" + brand.id.get } class="btn btn-small btn-info"><i class="icon-edit"></i></a> ++ Text(" ") ++
            link("/admin/brand/", () => { brand.delete_! }, <i class="icon-trash"></i>, "class" -> "btn btn-small btn-danger")
      }
    }

    val (searchType, keyword, status) = (S.param("type"), S.param("keyword"), S.param("status"))
    val by = keyword match {
      case Full(k) if (!k.trim().isEmpty()) =>
        val kv = k.trim()
        if (searchType == "0") {
          Full(By(Brand.regNo, kv))
        } else if (searchType == "1") {
          Full(By(Brand.owner, kv.toLong))
        } else {
          Empty
        }
      case _ => Empty
    }

    var searchTypeVal, keywordVal, statusVal = ""
    var url = "/admin/brand/"
    searchType match {
      case Full(t) =>
        searchTypeVal = t
        url = appendParams(url, List("type" -> t))
      case _ => ""
    }
    keyword match {
      case Full(k) if (!k.trim().isEmpty()) =>
        keywordVal = k
        url = appendParams(url, List("keyword" -> k))
      case _ => ""
    }
    status match {
      case Full(s) =>
        statusVal = s
        url = appendParams(url, List("status" -> s))
      case _ => ""
    }

    val paginatorModel = by match {
      case Full(realBy) => Brand.paginator(url, realBy, OrderBy(Brand.id, Descending))(itemsOnPage = 1)
      case _ => Brand.paginator(url, OrderBy(Brand.id, Descending))(itemsOnPage = 20)
    }

    val searchForm = "#searchForm" #>
      <form class="form-inline" action="/admin/brand/" method="get">
        <select id="searchType" name="type">
          <option value="0" selected={ if (searchTypeVal == "0") "selected" else null }>注册号</option>
          <option value="1" selected={ if (searchTypeVal == "1") "selected" else null }>用户ID</option>
        </select>
        <input type="text" id="keyword" name="keyword" value={ keywordVal }/>
        <select id="status" name="status">
          { for ((k, v) <- Brand.validStatusSelectValues) yield <option value={ k } selected={ if (statusVal == k) "selected" else null }>{ v }</option> }
        </select>
        <button type="submit" class="btn">搜索</button>
      </form>

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

    searchForm & dataList & paginator
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