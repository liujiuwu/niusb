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

object BrandOps {
  object brandVar extends RequestVar[Box[Brand]](Full(Brand.create))

  def add = {
    def process(): JsCmd = {
      println(dateFormatter.format(brandVar.get.get.regDate.get, "yyyy-MM-dd"))
      Noop
    }

    User.currentUser match {
      case Full(user) =>
      case _ => S.redirectTo("/")
    }

    val brand = brandVar.is.get
    "@regNo" #> text(brand.regNo.get, brand.regNo(_)) &
      "@basePrice" #> text(brand.basePrice.get.toString, basePrice => brand.basePrice(basePrice.toInt)) &
      "@name" #> text(brand.name.get, brand.name(_)) &
      "@regDate" #> text(brand.regDate.asHtml.toString, regDate => brand.regDate(new SimpleDateFormat("yyyy-MM-dd").parse(regDate))) &
      "@applicant" #> text(brand.applicant.get, brand.applicant(_)) &
      "@useDescn" #> textarea(brand.useDescn.get, brand.useDescn(_)) &
      "@descn" #> textarea(brand.descn.get, brand.descn(_)) &
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