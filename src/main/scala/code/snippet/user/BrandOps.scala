package code.snippet.user

import java.io.File
import scala.language.postfixOps
import scala.xml.NodeSeq
import scala.xml.Text
import org.apache.commons.io.FileUtils
import code.lib.BoxAlert
import code.lib.BoxConfirm
import code.lib.BrandType
import code.lib.BrandTypeHelper
import code.lib.SearchHelper
import code.lib.WebHelper
import code.model.Brand
import code.model.BrandStatus
import code.model.User
import code.rest.UploadManager
import code.snippet.SnippetHelper
import net.coobird.thumbnailator.Thumbnails
import net.liftweb.common.Box.box2Option
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.S
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.SHtml.a
import net.liftweb.http.SHtml.ajaxCall
import net.liftweb.http.SHtml.ajaxInvoke
import net.liftweb.http.SHtml.ajaxSubmit
import net.liftweb.http.SHtml.hidden
import net.liftweb.http.SHtml.select
import net.liftweb.http.SHtml.text
import net.liftweb.http.SHtml.textarea
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JE.ValById
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds
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
import net.liftweb.util.CssSel
import net.liftweb.util.Helpers.asLong
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import net.liftweb.util.Helpers.tryo
import code.snippet.PaginatorHelper
import net.liftweb.http.DispatchSnippet
import net.liftweb.mapper.QueryParam

class BrandOps extends DispatchSnippet with SnippetHelper with Loggable {
  def user = User.currentUser.openOrThrowException("not found user")

  def dispatch = {
    case "create" => create
    case "list" => list
    case "view" => view
    case "uploadBrandPic" => uploadBrandPic
    case "getRemoteData" => getRemoteData
  }

  def create = {
    var basePrice = "0"
    var regNo, pic, name, regDateStr, applicant, useDescn, descn = ""
    var brandType: BrandType = BrandTypeHelper.brandTypes.get(25).get

    def process(): JsCmd = {
      val brand = Brand.create.regNo(regNo).name(name).pic(pic).regDate(WebHelper.dateParse(regDateStr).openOrThrowException("商标注册日期错误")).applicant(applicant).useDescn(useDescn).descn(descn)
      brand.owner(user)
      brand.brandTypeId(brandType.id)
      brand.basePrice(tryo(basePrice.toInt).getOrElse(0))
      brand.sellPrice(brand.basePrice + (brand.basePrice.get * 0.1).toInt)
      brand.validate match {
        case Nil =>
          brand.save
          UploadManager.handleBrandImg(pic)
          S.redirectTo("/user/brand/")
        case errors => println(errors); Noop
      }
    }

    val brandTypes = BrandTypeHelper.brandTypes.values.toList
    "@regNo" #> text(regNo, regNo = _) &
      "@basePrice" #> text(basePrice, basePrice = _) &
      "@name" #> text(name, name = _) &
      "@pic" #> hidden(pic = _, pic) &
      "@brand_type" #> select(brandTypes.map(v => (v.id.toString, v.id + " -> " + v.name)), Empty, v => (brandType = BrandTypeHelper.brandTypes.get(v.toInt).get)) &
      "@regDate" #> text(regDateStr, regDateStr = _) &
      "@applicant" #> text(applicant, applicant = _) &
      "@useDescn" #> textarea(useDescn, useDescn = _) &
      "@descn" #> textarea(descn, descn = _) &
      "@sub" #> hidden(process)
  }

  def getRemoteData = {
    "* [onclick]" #> ajaxCall(ValById("regNo"),
      regNo => {
        //TODO 检查标号合法性
        //WebHelper.formError("regNo", "错误的商标注册号，请核实！")
        //TODO 从标局获取商标数据
        val data = SearchHelper.searchBrandByRegNo(regNo)
        val brandData = data
        if (brandData.isEmpty) {
          JsRaw(WebHelper.errorMsg("opt_brand_tip", Text("商标信息查询失败，请稍候再试！")))
        } else {
          val name = brandData.getOrElse("name", "")
          val flh = brandData.getOrElse("flh", "")
          val applicant = brandData.getOrElse("sqr", "")
          val zcggrq = brandData.getOrElse("zcggrq", "")
          val fwlb = brandData.getOrElse("fwlb", "")
          SetValById("name", name) & SetValById("applicant", applicant) & SetValById("brand_type", flh) & SetValById("stest", "2") &
            SetValById("regDate", zcggrq) & SetValById("useDescn", fwlb) & JsRaw("""$('#getRemoteData').removeClass("disabled")""")
        }
      })
  }

  private def bies: List[QueryParam[Brand]] = {
    List[QueryParam[Brand]](OrderBy(Brand.id, Descending), By(Brand.owner, user))
  }

  def list = {
    def actions(brand: Brand): NodeSeq = {
      brand.status.get match {
        case BrandStatus.ShenHeShiBai | BrandStatus.ShenHeZhong =>
          a(() => {
            BoxConfirm("确定删除【" + brand.name.get + "】商标？此操作不可恢复，请谨慎！", {
              ajaxInvoke(() => {
                brand.status.get match {
                  case BrandStatus.ShenHeShiBai | BrandStatus.ShenHeZhong =>
                    brand.delete_!
                    JsCmds.Reload
                  //JsCmds.After(3 seconds, JsCmds.Reload) //or whatever javascript response you want, e.g. JsCmds.Noop
                  case _ => BoxAlert("已审核通过商标无法进行删除操作！")
                }
              })._2
            })
          }, <i class="icon-trash"></i>, "class" -> "btn btn-danger")
        case _ => Text("")
      }
    }

    var url = "/user/brand/"
    val paginatorModel = Brand.paginator(url, bies: _*)()

    val dataList = "#dataList tr" #> paginatorModel.datas.map(brand => {
      "#regNo" #> brand.regNo &
        "#name" #> <a href={ "/user/brand/view?id=" + brand.id.get }>{ brand.name }</a> &
        "#brandType" #> brand.displayType &
        "#applicant" #> brand.applicant &
        "#regDate" #> brand.regDate.asHtml &
        "#status" #> brand.displayStatus &
        "#basePrice" #> brand.displayBasePrice &
        "#actions " #> actions(brand)
    })

    dataList & "#pagination" #> paginatorModel.paginate _
  }

  def view = {
    tabMenuRV(Full("zoom-in" -> "查看商标"))
    (for {
      brandId <- S.param("id").flatMap(asLong) ?~ "商标ID不存在或无效"
      brand <- Brand.find(By(Brand.owner, user), By(Brand.id, brandId)) ?~ s"ID为${brandId}的商标不存在。"
    } yield {
      "#regNo" #> brand.regNo &
        "#name" #> brand.name &
        "#pic" #> brand.displayPic() &
        "#spic" #> brand.displaySpic &
        "#brand-type" #> brand.displayType &
        "#status" #> brand.displayStatus &
        "#basePrice" #> brand.displayBasePrice &
        "#regdate" #> brand.regDate.asHtml &
        "#useDescn" #> brand.useDescn &
        "#descn" #> brand.descn
    }): CssSel
  }

  def uploadBrandPic = {
    var picName, x, y, w, h = ""
    def process(): JsCmd = {
      val uploadPic = new File(UploadManager.uploadTmpDir + File.separator + picName)
      val newPicName = UploadManager.sizePicName(picName)
      Thumbnails.of(uploadPic)
        .sourceRegion(x.toInt, y.toInt, w.toInt, h.toInt)
        .size(w.toInt, h.toInt)
        .outputQuality(1f)
        .toFile(new File(UploadManager.uploadTmpDir + File.separator + newPicName))

      FileUtils.deleteQuietly(uploadPic)
      val imgSrc = UploadManager.srcTmpPath(newPicName)
      JsRaw("$('#uploadDialog').modal('hide');$('#brand_pic').attr('src','" + imgSrc + "')")
    }

    "@picName" #> hidden(picName = _, picName) &
      "@x" #> hidden(x = _, x) &
      "@y" #> hidden(y = _, y) &
      "@w" #> hidden(w = _, w) &
      "@h" #> hidden(h = _, h) &
      "type=submit" #> ajaxSubmit("保存商标图", process)
  }
}