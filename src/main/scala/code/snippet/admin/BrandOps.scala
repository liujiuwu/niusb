package code.snippet.admin

import java.io.File
import scala.collection.mutable.ArrayBuffer
import scala.xml._
import org.apache.commons.io.FileUtils
import code.model.Brand
import code.model.BrandStatus
import code.model.BrandType
import code.snippet.SnippetHelper
import net.liftweb.common._
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import code.lib.WebCacheHelper
import com.niusb.util.WebHelpers._
import com.niusb.util.WebHelpers
import com.niusb.util.UploadHelpers

object BrandOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "list" => list
    case "view" => view
    case "edit" => edit
    case "sedit" => sedit
  }

  private def bies: List[QueryParam[Brand]] = {
    val (searchType, keyword, status) = (S.param("type"), S.param("keyword"), S.param("status"))
    val byBuffer = ArrayBuffer[QueryParam[Brand]](OrderBy(Brand.id, Descending))
    keyword match {
      case Full(k) if (!k.trim().isEmpty()) =>
        val kv = k.trim()
        searchType match {
          case Full("0") => byBuffer += By(Brand.regNo, kv)
          case Full("1") => byBuffer += By(Brand.owner, kv.toLong)
          case _ =>
        }
      case _ =>
    }

    status match {
      case Full(s) if (s != "all") =>
        byBuffer += By(Brand.status, BrandStatus(s.toInt))
      case _ =>
    }
    byBuffer.toList
  }

  def list = {
    def actions(brand: Brand): NodeSeq = {
      <a href={ "/admin/brand/sedit?id=" + brand.id.get } class="btn btn-primary"><i class="icon-bolt"></i></a> ++ Text(" ") ++
        <a href={ "/admin/brand/edit?id=" + brand.id.get } class="btn btn-info"><i class="icon-edit"></i></a> ++ Text(" ") ++
        a(() => {
          BoxConfirm("确定删除【" + brand.name.get + "】商标？此操作不可恢复，请谨慎！", {
            ajaxInvoke(() => { brand.delete_!; JsCmds.Reload })._2
          })
        }, <i class="icon-trash"></i>, "class" -> "btn btn-danger")
    }

    val (searchType, keyword, status) = (S.param("type"), S.param("keyword"), S.param("status"))

    var searchTypeVal, keywordVal, statusVal = ""
    var url = originalUri
    searchType match {
      case Full(t) =>
        searchTypeVal = t
        url = appendParams(url, List("type" -> t))
      case _ =>
    }
    keyword match {
      case Full(k) if (!k.trim().isEmpty()) =>
        keywordVal = k
        url = appendParams(url, List("keyword" -> k))
      case _ =>
    }
    status match {
      case Full(s) =>
        statusVal = s
        url = appendParams(url, List("status" -> s))
      case _ =>
    }

    val paginatorModel = Brand.paginator(url, bies: _*)()

    val searchForm = "#searchForm" #>
      <form class="form-inline" action={ url } method="get">
        <select id="searchType" name="type">
          <option value="0" selected={ if (searchTypeVal == "0") "selected" else null }>注册号</option>
          <option value="1" selected={ if (searchTypeVal == "1") "selected" else null }>用户ID</option>
        </select>
        <input type="text" id="keyword" name="keyword" value={ keywordVal }/>
        <select id="status" name="status">
          { for ((k, v) <- Brand.validStatusSelectValues) yield <option value={ k } selected={ if (statusVal == k) "selected" else null }>{ v }</option> }
        </select>
        <button type="submit" class="btn"><i class="icon-search"></i> 搜索</button>
      </form>

    val dataList = "#dataList tr" #> paginatorModel.datas.map(brand => {
      "#regNo" #> brand.regNo.get &
        "#name" #> <a href={ "/admin/brand/view?id=" + brand.id.get }>{ brand.name }</a> &
        "#brandType" #> brand.brandTypeCode.displayType &
        "#regDate" #> brand.regDate.asHtml &
        "#status" #> brand.status.displayStatus &
        "#basePrice" #> brand.basePrice.displayBasePrice &
        "#sellPrice" #> brand.sellPrice.displaySellPrice(false, true) &
        "#own" #> brand.isOwn.displayOwn &
        "#offer" #> brand.isOffer.displayOffer &
        "#recommend" #> brand.isRecommend.displayRecommend &
        "#strikePrice" #> brand.strikePrice.displayStrikePrice &
        "#actions" #> actions(brand)
    })
    searchForm & dataList & "#pagination" #> paginatorModel.paginate _
  }

  def view = {
    tabMenuRV(Full("zoom-in" -> "查看商标"))

    (for {
      brandId <- S.param("id").flatMap(asLong) ?~ "商标ID不存在或无效"
      brand <- Brand.find(By(Brand.id, brandId)) ?~ s"ID为${brandId}的商标不存在。"
    } yield {
      "#regNo" #> brand.regNo.get &
        "#name" #> brand.name.get &
        "#brand-type" #> brand.brandTypeCode.displayType &
        "#status" #> brand.status.displayStatus &
        "#basePrice" #> brand.basePrice.displayBasePrice &
        "#sellPrice" #> brand.sellPrice.displaySellPrice(style = true) &
        "#strikePrice" #> brand.strikePrice.displayStrikePrice &
        "#regdate" #> brand.regDate.asHtml &
        "#applicant" #> brand.applicant &
        "#useDescn" #> brand.useDescn &
        "#descn" #> brand.descn &
        "#own" #> brand.isOwn.displayOwn &
        "#recommend" #> brand.isRecommend.displayRecommend &
        "#offer" #> brand.isOffer.displayOffer &
        "#followCount" #> brand.followCount.get &
        "#viewCount" #> brand.viewCount.get &
        "#remark" #> brand.remark.get &
        "#pic" #> brand.pic.displayPic(alt = brand.name.get) &
        "#spic" #> brand.pic.displaySmallPic &
        "#owner" #> brand.owner.getOwner.displayInfo &
        "#edit-btn" #> <a href={ "/admin/brand/edit?id=" + brand.id.get } class="btn btn-primary"><i class="icon-edit"></i> 修改商标</a> &
        "#sedit-btn" #> <a href={ "/admin/brand/sedit?id=" + brand.id.get } class="btn btn-info"><i class="icon-bolt"></i> 商标设置</a> &
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
      var regNo, pic, name, regDateStr, applicant, useDescn, descn, lsqz = ""
      var brandType: BrandType = WebCacheHelper.brandTypes.get(brand.brandTypeCode.get).get

      def process(): JsCmd = {
        val oldPic = brand.pic.get
        brand.regNo(regNo).basePrice(basePrice.toInt).pic(pic).name(name).regDate(WebHelpers.dateParse(regDateStr).openOrThrowException("商标注册日期错误")).applicant(applicant).useDescn(useDescn).descn(descn)
        brand.brandTypeCode(brandType.code.get)
        brand.lsqz(lsqz)
        brand.validate match {
          case Nil =>
            brand.save
            UploadHelpers.handleBrandImg(pic)
            if (oldPic != pic) {
              val oldPicFile = new File(UploadHelpers.uploadBrandDir() + File.separator + oldPic)
              if (oldPicFile.exists()) {
                FileUtils.deleteQuietly(oldPicFile)
              }
            }
            //JsRaw(WebHelper.succMsg("opt_brand_tip", Text("商标信息已成功修改！")))
            S.redirectTo("/admin/brand/view?id=" + brand.id.get)
          case errors => println(errors); Noop
        }
      }

      val brandTypes = WebCacheHelper.brandTypes.values.toList
      "@regNo" #> text(brand.regNo.get, regNo = _) &
        "@basePrice" #> text(brand.basePrice.get.toString, basePrice = _) &
        "@name" #> text(brand.name.get, name = _) &
        "@pic" #> hidden(pic = _, brand.pic.get) &
        "#brand_pic [src]" #> brand.pic.src &
        "@brand_status" #> selectObj[BrandStatus.Value](BrandStatus.values.toList.map(v => (v, v.toString)), Full(brand.status.is), brand.status(_)) &
        "@brandType" #> select(brandTypes.map(v => (v.code.is.toString, v.code.is + " -> " + v.name.is)), Full(brandType.code.toString), v => (brandType = WebCacheHelper.brandTypes.get(v.toInt).get)) &
        "@regDate" #> text(brand.regDate.asHtml.text, regDateStr = _) &
        "@applicant" #> text(brand.applicant.get, applicant = _) &
        "@useDescn" #> textarea(brand.useDescn.get, useDescn = _) &
        "@descn" #> textarea(brand.descn.get, descn = _) &
        "@lsqz" #> hidden(lsqz = _, lsqz) &
        "#sedit-btn" #> <a href={ "/admin/brand/sedit?id=" + brand.id.get } class="btn btn-primary"><i class="icon-bolt"></i> 商标设置</a> &
        "#view-btn" #> <a href={ "/admin/brand/view?id=" + brand.id.get } class="btn btn-info"><i class="icon-info"></i> 查看商标</a> &
        "#list-btn" #> <a href="/admin/brand/" class="btn btn-success"><i class="icon-list"></i> 商标列表</a> &
        "type=submit" #> ajaxSubmit("保存修改", process)
    }): CssSel
  }

  def sedit = {
    tabMenuRV(Full("bolt" -> "商标设置"))

    (for {
      brandId <- S.param("id").flatMap(asLong) ?~ "商标ID不存在或无效"
      brand <- Brand.find(By(Brand.id, brandId)) ?~ s"ID为${brandId}的商标不存在。"
    } yield {
      var basePrice, sellPrice, strikePrice = "0"
      var recommend, own, offer = "0"
      var name, remark = ""

      def process(): JsCmd = {
        brand.basePrice(basePrice.toInt).sellPrice(sellPrice.toInt).strikePrice(strikePrice.toInt)
        brand.isRecommend(TrueOrFalse(recommend)).isOwn(TrueOrFalse(own)).isOffer(TrueOrFalse(offer))
        brand.remark(remark)
        brand.save
        S.redirectTo("/admin/brand/view?id=" + brand.id.get)
      }

      "@name" #> text(brand.name.get, name = _, "disabled" -> "disabled") &
        "@brand_status" #> selectObj[BrandStatus.Value](BrandStatus.values.toList.map(v => (v, v.toString)), Full(brand.status.is), brand.status(_)) &
        "@recommend" #> select(TrueOrFalse.selectTrueOrFalse, TrueOrFalse2Str(brand.isRecommend.get), recommend = _) &
        "@own" #> select(TrueOrFalse.selectTrueOrFalse, TrueOrFalse2Str(brand.isOwn.get), own = _) &
        "@offer" #> select(TrueOrFalse.selectTrueOrFalse, TrueOrFalse2Str(brand.isOffer.get), offer = _) &
        "@basePrice" #> text(brand.basePrice.get.toString, basePrice = _) &
        "@sellPrice" #> text(brand.sellPrice.get.toString, sellPrice = _) &
        "#realSellPrice" #> brand.sellPrice.displaySellPrice(false) &
        "@strikePrice" #> text(brand.strikePrice.get.toString, strikePrice = _) &
        "@remark" #> textarea(brand.remark.get, remark = _) &
        "#edit-btn" #> <a href={ "/admin/brand/edit?id=" + brand.id.get } class="btn btn-primary"><i class="icon-edit"></i> 修改商标</a> &
        "#view-btn" #> <a href={ "/admin/brand/view?id=" + brand.id.get } class="btn btn-info"><i class="icon-info"></i> 查看商标</a> &
        "#list-btn" #> <a href="/admin/brand/" class="btn btn-success"><i class="icon-list"></i> 商标列表</a> &
        "type=submit" #> ajaxSubmit("保存修改", process)
    }): CssSel
  }

}