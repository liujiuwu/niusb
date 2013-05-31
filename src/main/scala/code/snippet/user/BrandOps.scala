package code.snippet.user

import java.text.SimpleDateFormat
import code.model.Brand
import code.model.User
import net.liftweb.common.Box
import net.liftweb.common.Full
import net.liftweb.http.RequestVar
import net.liftweb.http.S
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JE.ValById
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmd._
import net.liftweb.http.js.JsCmds._
import net.liftweb.util.Helpers._
import code.lib.WebHelper
import net.liftweb.util.TimeHelpers

object BrandOps {
  //object brandVar extends RequestVar[Box[Brand]](Full(Brand.create))

  def add = {
    var basePrice = "0"
    var regNo, name, regDateStr, applicant, useDescn, descn = ""

    def process(): JsCmd = {
      val brand = Brand.create.regNo(regNo).name(name).regDate(WebHelper.dateParse(regDateStr).openOrThrowException("商标注册日期错误")).applicant(applicant).useDescn(useDescn).descn(descn)
      brand.validate match {
        case Nil => brand.save
        case errors => println(errors)
      }

      Noop
    }

    User.currentUser match {
      case Full(user) =>
      case _ => S.redirectTo("/")
    }

    "@regNo" #> text(regNo, regNo = _) &
      "@basePrice" #> text(basePrice, basePrice = _) &
      "@name" #> text(name, name = _) &
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
        Noop
      })
  }
}