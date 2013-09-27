package code.snippet

import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import code.model.AdSpace
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml._
import scala.xml._
import net.liftweb.common._
import net.liftweb.util.CssSel
import code.lib.WebCacheHelper
import code.model.BrandType
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import com.niusb.util.WebHelpers
import code.model.BrandApplication
import com.niusb.util.BootBoxHelpers
import net.liftweb.http.js.JE.JsRaw
import code.model.BrandApplicationStatus
import code.model.User
import net.liftweb.mapper.By

object BrandOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "brandApplication" => brandApplication
  }

  def brandApplication = {
    var brandName, brandTypeCode, name, contactInfo, additional = ""
    var brandType: Option[BrandType] = Empty

    User.currentUser match {
      case Full(user) =>
        User.find(By(User.id, user.id.is)) match {
          case Full(u) =>
            name = u.name.is
            contactInfo = u.mobile.is
          case _ =>
        }
      case _ =>
    }

    def process(): JsCmd = {
      val brandApplication = BrandApplication.create

      if (brandName.trim.isEmpty()) {
        return WebHelpers.formError("brand-name", "请输入您要申请注册的商标名")
      } else if (brandName.trim.length() > 15) {
        return WebHelpers.formError("brand-name", "申请注册的商标名15字以内")
      } else {
        brandApplication.brandName(brandName.trim)
      }

      if (name.trim.isEmpty()) {
        return WebHelpers.formError("name", "请输入联系人姓名")
      } else if (name.trim.length() > 50) {
        return WebHelpers.formError("name", "联系人姓名50字以内")
      } else {
        brandApplication.name(name.trim)
      }

      if (contactInfo.trim.isEmpty()) {
        return WebHelpers.formError("name", "请输入联系人的联系方式")
      } else if (contactInfo.trim.length() > 100) {
        return WebHelpers.formError("name", "联系人的联系方式在100字以内")
      } else {
        brandApplication.contactInfo(contactInfo.trim)
      }

      if (!additional.trim.isEmpty() && additional.trim.length() > 300) {
        return WebHelpers.formError("name", "附加说明，请控制在300字以内。")
      } else {
        brandApplication.additional(additional.trim)
      }

      User.currentUser match {
        case Full(user) => brandApplication.owner(user)
        case _ =>
      }
      brandApplication.brandTypeCode(brandType.get.code.is)
      brandApplication.status(BrandApplicationStatus.ShenHeZhong)
      brandApplication.save
      JsRaw("""$("#brand-application-dialog").modal('hide')""") & BootBoxHelpers.BoxAlert("您的商标注册申请信息已经成功提交，稍候我们将与您联系核实商标注册资料，谢谢您的合作与支持！", Reload)
    }

    val brandTypes = WebCacheHelper.brandTypes.values.toList
    "@brandName" #> text(brandName, brandName = _) &
      "@brandType" #> select(brandTypes.map(v => (v.code.is.toString, v.code.is + " -> " + v.name.is)), Empty, v => (brandType = WebCacheHelper.brandTypes.get(v.toInt))) &
      "@name" #> text(name, name = _) &
      "@contactInfo" #> textarea(contactInfo, contactInfo = _) &
      "@additional" #> textarea(additional, additional = _) &
      "type=submit" #> ajaxSubmit("确认提交申请", process)
  }

}