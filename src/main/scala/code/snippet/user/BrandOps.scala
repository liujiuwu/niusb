package code.snippet.user

import code.lib.BrandType
import code.lib.SearchHelper
import code.lib.WebHelper
import code.model.Brand
import code.model.User
import code.snippet.MyPaginatorSnippet
import net.liftweb.common.Box
import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmd._
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper.By
import net.liftweb.mapper.MaxRows
import net.liftweb.mapper.StartAt
import net.liftweb.util.Helpers._
import scala.xml.Text
import code.lib.BrandTypeHelper
import net.liftweb.common.Empty
import code.model.BrandStatus
import scala.xml.NodeSeq
import net.liftweb.mapper.OrderBy
import net.liftweb.mapper.Ascending
import net.liftweb.mapper.Descending

object BrandOps extends MyPaginatorSnippet[Brand] {
  //object brandVar extends RequestVar[Box[Brand]](Full(Brand.create))
  val userId = User.currentUserId.map(_.toLong).openOr(0L)
  override def itemsPerPage = 10
  override def count = Brand.count(By(Brand.userId, userId))
  override def page = Brand.findAll(By(Brand.userId, userId), StartAt(curPage * itemsPerPage), MaxRows(itemsPerPage), OrderBy(Brand.createdAt, Descending))

  def add = {
    var basePrice = "0"
    var regNo, name, regDateStr, applicant, useDescn, descn = ""
    var brandType: BrandType = BrandTypeHelper.brandTypes.get(25).get

    def process(): JsCmd = {
      val brand = Brand.create.regNo(regNo).name(name).regDate(WebHelper.dateParse(regDateStr).openOrThrowException("商标注册日期错误")).applicant(applicant).useDescn(useDescn).descn(descn)
      brand.userId(User.currentUserId.map(_.toInt).openOrThrowException("user id error"))
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
      "@brand_type" #> selectObj[BrandType](brandTypes.map(v => (v, v.id + " -> " + v.name)), Empty, brandType = _) &
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
          SetValById("name", name) & SetValById("applicant", applicant) &
            SetValById("regDate", zcggrq) & SetValById("useDescn", fwlb) & JsRaw("""$('#getRemoteData').removeClass("disabled")""")
        }
      })
  }

  def getBrandPic = {
    val regNo = S.param("regNo") openOr ("")
    SearchHelper.searchBrandPicByRegNo(regNo)
  }

  def list = {
    def statusLabel(status: BrandStatus.Value): NodeSeq = {
      status match {
        case BrandStatus.ShenHeShiBai => <span class="label label-important">审核失败</span>
        case BrandStatus.ShenHeZhong => <span class="label">审核中</span>
        case BrandStatus.ChuShoZhong => <span class="label label-info">出售中</span>
        case BrandStatus.JiaoYiZhong => <span class="label label-info">交易中</span>
        case BrandStatus.JiaoYiChengGong => <span class="label label-success">交易成功</span>
      }
    }

    var odd = "even"
    "tr" #> page.map {
      b =>
        val brandType = BrandTypeHelper.brandTypes.get(b.brandTypeId.get).get
        odd = WebHelper.oddOrEven(odd)
        "tr [class]" #> odd &
          "#regNo" #> b.regNo &
          "#name" #> b.name &
          "#brandType" #> { brandType.id + " -> " + brandType.name } &
          "#regDate" #> b.regDate.asHtml &
          "#status" #> statusLabel(b.status.get) &
          "#basePrice" #> b.basePrice &
          "#regNo" #> b.regNo
    }
  }

}