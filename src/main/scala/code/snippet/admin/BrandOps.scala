package code.snippet.admin

import java.io.File
import scala.collection.mutable.ArrayBuffer
import scala.xml.NodeSeq
import scala.xml.Text
import org.apache.commons.io.FileUtils
import com.niusb.util.UploadHelpers
import com.niusb.util.WebHelpers.BoxConfirm
import com.niusb.util.WebHelpers.TrueOrFalse
import com.niusb.util.WebHelpers.TrueOrFalse2Str
import code.lib.WebCacheHelper
import code.model._
import code.snippet.SnippetHelper
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.SHtml.a
import net.liftweb.http.SHtml.ajaxInvoke
import net.liftweb.http.SHtml.ajaxSubmit
import net.liftweb.http.SHtml.hidden
import net.liftweb.http.SHtml.select
import net.liftweb.http.SHtml.selectObj
import net.liftweb.http.SHtml.text
import net.liftweb.http.SHtml.textarea
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmds.jsExpToJsCmd
import net.liftweb.mapper.By
import net.liftweb.mapper.Descending
import net.liftweb.mapper.OrderBy
import net.liftweb.mapper.QueryParam
import net.liftweb.util.CssSel
import net.liftweb.util.Helpers.appendParams
import net.liftweb.util.Helpers.asLong
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import com.niusb.util.WebHelpers
import code.model.BrandStatus
import com.niusb.util.SearchBrandFormHelpers
import net.liftweb.mapper._

object BrandOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "list" => list
    case "view" => view
    case "edit" => edit
    case "sedit" => sedit
    case "application" => application
  }

  private def bies: List[QueryParam[Brand]] = {
    val (searchType, keyword, status, brandTypeCode, orderBy) = (S.param("type"), S.param("keyword"), S.param("status"), S.param("brandTypeCode"), S.param("orderBy"))
    val byBuffer = ArrayBuffer[QueryParam[Brand]]()
    keyword match {
      case Full(k) if (!k.trim().isEmpty()) =>
        val kv = k.trim()
        searchType match {
          case Full("0") => byBuffer += Like(Brand.name, s"%${kv}%")
          case Full("1") => byBuffer += By(Brand.regNo, kv)
          case Full("2") => byBuffer += By(Brand.id, kv.toLong)
          case Full("3") => byBuffer += By(Brand.owner, kv.toLong)
          case _ =>
        }
      case _ =>
    }

    status match {
      case Full(s) if (s != "all") =>
        byBuffer += By(Brand.status, BrandStatus(s.toInt))
      case _ =>
    }

    brandTypeCode match {
      case Full(s) if (s != "all") =>
        byBuffer += By(Brand.brandTypeCode, s.toInt)
      case _ =>
    }

    orderBy match {
      case Full(ob) =>
        ob.toInt match {
          case 0 => byBuffer += OrderBy(Brand.id, Descending)
          case 1 => byBuffer += OrderBy(Brand.basePrice, Ascending)
          case 2 => byBuffer += OrderBy(Brand.basePrice, Descending)
          case 3 => byBuffer += OrderBy(Brand.viewCount, Descending)
          case 4 => byBuffer += By_>(Brand.followCount, 0)
          case 5 => byBuffer += By(Brand.isRecommend, true)
          case 6 => byBuffer += By(Brand.isOwn, true)
          case 7 => byBuffer += By(Brand.isOffer, true)
        }
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
            ajaxInvoke(() => {
              brand.delBrandAndUpdateBrandType
              JsCmds.Reload
            })._2
          })
        }, <i class="icon-trash"></i>, "class" -> "btn btn-danger")
    }

    val (searchType, keyword, status, brandTypeCode, orderBy) = (S.param("type"), S.param("keyword"), S.param("status"), S.param("brandTypeCode"), S.param("orderBy"))

    var searchTypeVal, keywordVal, statusVal, brandTypeCodeVal, orderByVal = ""
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
    brandTypeCode match {
      case Full(bt) =>
        brandTypeCodeVal = bt
        url = appendParams(url, List("brandTypeCode" -> bt))
      case _ =>
    }
    orderBy match {
      case Full(ob) =>
        orderByVal = ob
        url = appendParams(url, List("orderBy" -> ob))
      case _ =>
    }

    val paginatorModel = Brand.paginator(url, bies: _*)()

    val searchForm = "#searchForm" #>
      <form class="form-inline" action={ url } method="get">
        <div class="form-group">
          <select class="form-control" id="brandTypeCode" name="brandTypeCode" style="width:150px">
            { for ((k, v) <- Brand.validBrandTypeSelectValues) yield <option value={ k } selected={ if (brandTypeCodeVal == k) "selected" else null }>{ v }</option> }
          </select>
        </div>
        <div class="form-group">
          <select class="form-control" id="status" name="status" style="width:150px">
            { for ((k, v) <- Brand.validStatusSelectValues) yield <option value={ k } selected={ if (statusVal == k) "selected" else null }>{ v }</option> }
          </select>
        </div>
        <div class="form-group">
          <select class="form-control" id="searchType" name="type" style="width:120px">
            { for ((k, v) <- SearchBrandFormHelpers.adminKeywordTypes) yield <option value={ k } selected={ if (searchTypeVal == k) "selected" else null }>{ v }</option> }
          </select>
        </div>
        <div class="form-group">
          <input type="text" class="form-control" id="keyword" name="keyword" value={ keywordVal } style="width:250px"/>
        </div>
        <div class="form-group">
          <select class="form-control" id="orderBy" name="orderBy" style="width:150px">
            { for ((k, v) <- Brand.validOrderBySelectValues) yield <option value={ k } selected={ if (orderByVal == k) "selected" else null }>{ v }</option> }
          </select>
        </div>
        <button type="submit" class="btn btn-primary"><i class="icon-search"></i> 搜索</button>
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
        "#descn" #> brand.descn.display &
        "#own" #> brand.isOwn.displayOwn &
        "#recommend" #> brand.isRecommend.displayRecommend &
        "#offer" #> brand.isOffer.displayOffer &
        "#followCount" #> brand.followCount.get &
        "#viewCount" #> brand.viewCount.get &
        "#remark" #> brand.remark.get &
        "#pic" #> brand.pic.displayPic(alt = brand.name.get) &
        "#spic" #> brand.pic.displaySmallPic &
        "#owner" #> brand.owner.display &
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
      var newStatus = BrandStatus.ShenHeZhong

      def process(): JsCmd = {
        val oldPic = brand.pic.get
        brand.regNo(regNo).basePrice(basePrice.toInt).pic(pic).name(name).regDate(WebHelpers.dateParse(regDateStr).openOrThrowException("商标注册日期错误")).applicant(applicant).useDescn(useDescn).descn(descn)
        brand.brandTypeCode(brandType.code.get)
        brand.lsqz(lsqz)
        brand.validate match {
          case Nil =>
            brand.saveAndUpdateBrandType(newStatus)
            brand.save
            UploadHelpers.handleBrandImg(pic)
            if (oldPic != pic) {
              val oldPicFile = new File(UploadHelpers.uploadBrandDir() + File.separator + oldPic)
              if (oldPicFile.exists()) {
                FileUtils.deleteQuietly(oldPicFile)
              }
            }
            S.redirectTo("/admin/brand/view?id=" + brand.id.is)
          case errors => logger.error(s"editBrand|${errors}")
        }
      }

      val brandTypes = WebCacheHelper.brandTypes.values.toList
      "@regNo" #> text(brand.regNo.is, regNo = _) &
        "@basePrice" #> text(brand.basePrice.is.toString, basePrice = _) &
        "@name" #> text(brand.name.is, name = _) &
        "@pic" #> hidden(pic = _, brand.pic.is) &
        "#brandPic [src]" #> brand.pic.src &
        "@brand_status" #> selectObj[BrandStatus.Value](BrandStatus.values.toList.map(v => (v, v.toString)), Full(brand.status.is), newStatus = _) &
        "@brandType" #> select(brandTypes.map(v => (v.code.is.toString, v.code.is + " -> " + v.name.is)), Full(brandType.code.toString), v => (brandType = WebCacheHelper.brandTypes.get(v.toInt).get)) &
        "@regDate" #> text(brand.regDate.asHtml.text, regDateStr = _) &
        "@applicant" #> text(brand.applicant.is, applicant = _) &
        "@useDescn" #> textarea(brand.useDescn.is, useDescn = _) &
        "@descn" #> textarea(brand.descn.is, descn = _) &
        "@lsqz" #> hidden(lsqz = _, lsqz) &
        "#sedit-btn" #> <a href={ "/admin/brand/sedit?id=" + brand.id.is } class="btn btn-primary"><i class="icon-bolt"></i> 商标设置</a> &
        "#view-btn" #> <a href={ "/admin/brand/view?id=" + brand.id.is } class="btn btn-info"><i class="icon-info"></i> 查看商标</a> &
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
      var newStatus = BrandStatus.ShenHeZhong

      def process(): JsCmd = {
        brand.basePrice(basePrice.toInt).sellPrice(sellPrice.toInt).strikePrice(strikePrice.toInt)
        brand.isRecommend(TrueOrFalse(recommend)).isOwn(TrueOrFalse(own)).isOffer(TrueOrFalse(offer))
        brand.remark(remark)
        brand.saveAndUpdateBrandType(newStatus)
        WebCacheHelper.loadIndexTabBrands()
        WebCacheHelper.loadIndexBrandTypeBrands()
        S.redirectTo("/admin/brand/view?id=" + brand.id.get)
      }

      "#name *" #> brand.name.is &
        "@brand_status" #> selectObj[BrandStatus.Value](BrandStatus.values.toList.map(v => (v, v.toString)), Full(brand.status.is), newStatus = _) &
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

  def application = {
    def actions(brandApplication: BrandApplication): NodeSeq = {
      a(() => {
        BoxConfirm("确定删除【" + brandApplication.name.is + "】商标注册申请？此操作不可恢复，请谨慎！", {
          ajaxInvoke(() => {
            brandApplication.delete_!
            JsCmds.Reload
          })._2
        })
      }, <i class="icon-trash"></i>, "class" -> "btn btn-danger btn-xs")
    }

    val byBuffer = ArrayBuffer[QueryParam[BrandApplication]](OrderBy(BrandApplication.id, Descending))
    var url = originalUri
    val paginatorModel = BrandApplication.paginator(url, byBuffer.toList: _*)()

    val dataList = "#dataList tr" #> paginatorModel.datas.map(brandApplication => {
      val brandType = WebCacheHelper.brandTypes.get(brandApplication.brandTypeCode.is).get
      "#applicationId" #> brandApplication.id.is &
        "#brandName" #> brandApplication.brandName.is &
        "#brandType" #> brandType.name.display() &
        "#name" #> brandApplication.name.is &
        "#contactInfo" #> brandApplication.contactInfo.is &
        "#status" #> brandApplication.status.asHtml &
        "#createdAt" #> brandApplication.createdAt.asHtml &
        "#actions " #> actions(brandApplication)
    })

    dataList & "#pagination" #> paginatorModel.paginate _
  }

}