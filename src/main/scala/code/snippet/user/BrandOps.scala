package code.snippet.user

import java.io.File
import scala.language.postfixOps
import scala.xml._
import org.apache.commons.io.FileUtils
import code.lib.BoxAlert
import code.lib.BoxConfirm
import code.lib.SearchHelper
import code.lib.WebHelper
import code.model.Brand
import code.model.BrandStatus
import code.model.User
import code.rest.UploadManager
import code.snippet.SnippetHelper
import net.coobird.thumbnailator.Thumbnails
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JE._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsExp._
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import code.snippet.PaginatorHelper
import net.liftweb.http.DispatchSnippet
import net.liftweb.mapper.QueryParam
import scala.collection.mutable.ArrayBuffer
import code.lib.UploadFileHelper
import code.model.BrandType

object BrandOps extends DispatchSnippet with SnippetHelper with Loggable {
  def user = User.currentUser.openOrThrowException("not found user")

  def dispatch = {
    case "create" => create
    case "list" => list
    case "view" => view
    case "uploadBrandPic" => uploadBrandPic
    case "queryRemoteData" => queryRemoteData
  }

  def create = {
    var basePrice = "0"
    var regNo, pic, name, regDateStr, applicant, useDescn, descn, lsqz = ""
    var brandType: BrandType = BrandType.getBrandTypes().get(25).get

    def process(): JsCmd = {
      val brand = Brand.create.regNo(regNo).name(name).pic(pic).regDate(WebHelper.dateParse(regDateStr).openOrThrowException("商标注册日期错误")).applicant(applicant).useDescn(useDescn).descn(descn)
      brand.owner(user)
      brand.brandTypeCode(brandType.code.get)
      brand.basePrice(tryo(basePrice.toInt).getOrElse(0))
      brand.sellPrice(brand.basePrice + (brand.basePrice.get * 0.1).toInt)
      brand.lsqz(lsqz)
      brand.validate match {
        case Nil =>
          brand.save
          UploadFileHelper.handleBrandImg(pic)
          S.redirectTo("/user/brand/")
        case errors => println(errors); Noop
      }
    }

    val brandTypes = BrandType.getBrandTypes().values.toList
    "@regNo" #> text(regNo, regNo = _) &
      "@basePrice" #> text(basePrice, basePrice = _) &
      "@name" #> text(name, name = _) &
      "@pic" #> hidden(pic = _, pic) &
      "@brand_type" #> select(brandTypes.map(v => (v.code.toString, v.code + " -> " + v.name)), Empty, v => (brandType = BrandType.getBrandTypes().get(v.toInt).get)) &
      "@regDate" #> text(regDateStr, regDateStr = _) &
      "@applicant" #> text(applicant, applicant = _) &
      "@useDescn" #> textarea(useDescn, useDescn = _) &
      "@descn" #> textarea(descn, descn = _) &
      "@lsqz" #> hidden(lsqz = _, lsqz) &
      "@sub" #> hidden(process)
  }

  def queryRemoteData = {
    "@queryRemoteData [onclick]" #> ajaxCall(ValById("regNo"),
      regNo => {
        //TODO 检查标号合法性
        //WebHelper.formError("regNo", "错误的商标注册号，请核实！")
        //TODO 从标局获取商标数据
        val brandData = SearchHelper.searchBrandByRegNo(regNo)
        if (brandData.isEmpty) {
          JsRaw(WebHelper.errorMsg("opt_brand_tip", Text("商标信息查询失败，请稍候再试！")))
        } else {
          val name = brandData.getOrElse("name", "")
          val flh = brandData.getOrElse("flh", "")
          val applicant = brandData.getOrElse("sqr", "")
          val zcggrq = brandData.getOrElse("zcggrq", "")
          val fwlb = brandData.getOrElse("fwlb", "")
          val lsqz = brandData.getOrElse("lsqz", "")
          SetValById("name", name) & SetValById("applicant", applicant) & SetValById("brand_type", flh) & SetValById("stest", "2") &
            SetValById("regDate", zcggrq) & SetValById("useDescn", fwlb) & SetValById("lsqz", lsqz) & JsRaw("""$('#queryRemoteData').removeClass("disabled")""")
        }
      })
  }

  private def bies: List[QueryParam[Brand]] = {
    val (searchType, keyword) = (S.param("type"), S.param("keyword"))
    val byBuffer = ArrayBuffer[QueryParam[Brand]](OrderBy(Brand.id, Descending), By(Brand.owner, user))
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
    byBuffer.toList
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

    val (searchType, keyword) = (S.param("type"), S.param("keyword"))

    var searchTypeVal, keywordVal = ""
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

    val paginatorModel = Brand.paginator(url, bies: _*)()

    val searchForm = "#searchForm" #>
      <form class="form-inline" action={ url } method="get">
        <select id="searchType" name="type">
          <option value="0" selected={ if (searchTypeVal == "0") "selected" else null }>注册号</option>
        </select>
        <input type="text" id="keyword" name="keyword" value={ keywordVal }/>
        <button type="submit" class="btn"><i class="icon-search"></i> 搜索</button>
      </form>

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

    searchForm & dataList & "#pagination" #> paginatorModel.paginate _
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
      val uploadPic = new File(UploadFileHelper.uploadTmpDir + File.separator + picName)
      Thumbnails.of(uploadPic)
        .sourceRegion(x.toInt, y.toInt, w.toInt, h.toInt)
        .size(w.toInt, h.toInt)
        .outputQuality(1f)
        .toFile(uploadPic)

      //FileUtils.deleteQuietly(uploadPic)
      val imgSrc = UploadFileHelper.srcTmpPath(picName)
      JsRaw("$('#uploadDialog').modal('hide');$('#brandPic').attr('src','" + imgSrc + "')")
    }

    "@picName" #> hidden(picName = _, picName) &
      "@x" #> hidden(x = _, x) &
      "@y" #> hidden(y = _, y) &
      "@w" #> hidden(w = _, w) &
      "@h" #> hidden(h = _, h) &
      "type=submit" #> ajaxSubmit("保存商标图", process)
  }
}