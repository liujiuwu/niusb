package code.snippet.user

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
import net.liftweb.common.Full
import net.liftweb.common.Empty
import net.liftweb.http.RequestVar
import net.liftweb.http.S
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmd._
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper.By
import net.liftweb.mapper.Descending
import net.liftweb.mapper.MaxRows
import net.liftweb.mapper.OrderBy
import net.liftweb.mapper.StartAt
import net.liftweb.util.Helpers._
import code.snippet.TabMenu
import net.liftweb.json.JsonDSL._
import code.rest.UploadManager
import java.io.File
import net.coobird.thumbnailator.Thumbnails

object BrandOps extends TabMenu with MyPaginatorSnippet[Brand] {
  object brandRV extends RequestVar[Box[Brand]](Empty)
  def user = User.currentUser.openOrThrowException("not found user")
  override def itemsPerPage = 10
  override def count = Brand.count(By(Brand.owner, user))
  override def page = Brand.findAll(By(Brand.owner, user), StartAt(curPage * itemsPerPage), MaxRows(itemsPerPage), OrderBy(Brand.createdAt, Descending))

  def add = {
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
          //JsRaw(WebHelper.succMsg("opt_brand_tip", Text("商标信息发布成功，请待审核！")))
          S.redirectTo("/user/brand/")
        case errors => println(errors); Noop
      }
    }

    val brandTypes = BrandTypeHelper.brandTypes.values.toList
    "@regNo" #> text(regNo, regNo = _) &
      "@basePrice" #> text(basePrice, basePrice = _) &
      "@name" #> text(name, name = _) &
      "@pic" #> hidden(pic = _, pic) &
      //"@brand_type" #> selectObj[BrandType](brandTypes.map(v => (v, v.id + " -> " + v.name)), Empty, brandType = _) &
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

  def getBrandPic = {
    val regNo = S.param("regNo") openOr ("")
    SearchHelper.searchBrandPicByRegNo(regNo)
  }

  def list = {
    def actions(brand: Brand): NodeSeq = {
      val viewLink = link("/user/brand/view",
        () => brandRV(Full(brand)), <i class="icon-zoom-in"></i>, "class" -> "btn btn-small btn-success")
      brand.status.get match {
        case BrandStatus.ShenHeShiBai | BrandStatus.ShenHeZhong =>
          viewLink ++ Text(" ") ++
            link("/user/brand/edit",
              () => brandRV(Full(brand)), <i class="icon-edit"></i>, "class" -> "btn btn-small btn-info") ++ Text(" ") ++
              link("/user/brand/", () => { brand.delete_! }, <i class="icon-trash"></i>, "class" -> "btn btn-small btn-danger")
        case _ => viewLink
      }
    }

    "tr" #> page.map(brand => {
      val brandType = BrandTypeHelper.brandTypes.get(brand.brandTypeId.get).get
      "#regNo" #> brand.regNo &
        "#name" #> brand.name &
        "#brandType" #> { brandType.id + " -> " + brandType.name } &
        "#applicant" #> brand.applicant &
        "#regDate" #> brand.regDate.asHtml &
        "#status" #> WebHelper.statusLabel(brand.status.get) &
        "#basePrice" #> <span class="badge badge-success">￥{ brand.basePrice }</span> &
        "#actions" #> actions(brand)
    })
  }
  
  def st(picName:String):String = {
     val scalePicNameReg = """([\w]+).(jpg|jpeg|png)""".r
    var newPicName = picName
      picName match {
        case scalePicNameReg(f, e) => newPicName = (f + "x320." + e)
        case _ => newPicName = picName
      }
    newPicName
  }

  def view = {
    tabMenuRV(Full("zoom-in", "查看商标"))
    val brand = brandRV.is.get
    val brandType = BrandTypeHelper.brandTypes.get(brand.brandTypeId.get).get

    
    
    "#regNo" #> brand.regNo &
      "#name" #> brand.name &
      "#pic" #> <img src={ "/upload/" + st(brand.pic.get) }/> &
      "#brand-type" #> { brandType.id + " -> " + brandType.name } &
      "#status" #> WebHelper.statusLabel(brand.status.get) &
      "#basePrice" #> <span class="badge badge-success">￥{ brand.basePrice }</span> &
      "#regdate" #> brand.regDate.asHtml &
      "#applicant" #> brand.applicant &
      "#useDescn" #> brand.useDescn &
      "#descn" #> brand.descn
  }

  def uploadBrandPic = {
    var picName, x, y, w, h = ""
    def process(): JsCmd = {
      println(picName + "|" + x + "|" + y)
      val uploadPic = UploadManager.getUploadDirTmp + File.separator + picName
      val scalePicNameReg = """([\w]+).(jpg|jpeg|png)""".r
      var newPicName = picName
      picName match {
        case scalePicNameReg(f, e) => newPicName = (f + "x320." + e)
        case _ => newPicName = picName
      }

      val saveUploadPic = UploadManager.getUploadDir + File.separator + newPicName
      Thumbnails.of(uploadPic)
        .sourceRegion(x.toInt, y.toInt, w.toInt, h.toInt)
        .size(w.toInt, h.toInt)
        .outputQuality(1f)
        .toFile(new File(saveUploadPic));

      SetHtml("result", Text(x))

    }

    "@picName" #> hidden(picName = _, picName) &
      "@x" #> hidden(x = _, x) &
      "@y" #> hidden(y = _, y) &
      "@w" #> hidden(w = _, w) &
      "@h" #> hidden(h = _, h) &
      "type=submit" #> ajaxSubmit("保存商标图", process)
  }

}