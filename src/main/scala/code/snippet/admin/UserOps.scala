package code.snippet.admin

import scala.xml.Text
import code.model.User
import code.snippet.MyPaginatorSnippet
import code.snippet.TabMenu
import net.liftweb.common._
import net.liftweb.http.RequestVar
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmd._
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper.Descending
import net.liftweb.mapper.MaxRows
import net.liftweb.mapper.OrderBy
import net.liftweb.mapper.StartAt
import net.liftweb.util.Helpers._
import net.liftweb.common.Empty
import scala.xml.NodeSeq
import net.liftweb.mapper.Genders
import code.lib.WebHelper
import net.liftweb.http.js.JsCmd
import code.model.UserType
import code.model.UserStatus

object userRV extends RequestVar[Box[User]](Empty)
object UserOps extends TabMenu with MyPaginatorSnippet[User] {

  override def itemsPerPage = 10
  override def count = {
    println("==============================************")
    User.count()}
  override def page = User.findAll(StartAt(curPage * itemsPerPage), MaxRows(itemsPerPage), OrderBy(User.createdAt, Descending))

  private def isSuperUserLabel(user: User) = {
    if (user.superUser.get) <span class="badge badge-success">是</span> else <span class="badge badge-important">否</span>
  }

  def list = {
    def actionsBtn(user: User): NodeSeq = {
      link("/admin/user/view",
        () => userRV(Full(user)), <i class="icon-zoom-in"></i>, "class" -> "btn btn-small btn-success") ++ Text(" ") ++
        link("/admin/user/edit",
          () => userRV(Full(user)), <i class="icon-edit"></i>, "class" -> "btn btn-small btn-info") ++ Text(" ") ++
          link("/admin/user/", () => { user.delete_! }, <i class="icon-trash"></i>, "class" -> "btn btn-small btn-danger")
    }

    "tr" #> page.map(user => {
      "#id" #> user.id &
        "#mobile" #> user.mobile.get &
        "#name" #> user.name.get &
        "#gender" #> user.gender &
        "#type" #> user.userType &
        "#email" #> user.email.get &
        "#enabled" #> user.enabled &
        "#isAdmin" #> isSuperUserLabel(user) &
        "#brandCount" #> link("/admin/brand/", () => userRV(Full(user)), Text(user.brandCount.toString), "class" -> "badge badge-success") &
        "#actions" #> actionsBtn(user)
    })
  }

  def view = {
    tabMenuRV(Full("zoom-in", "查看用户"))
    val user = userRV.is.get

    "#id" #> user.id &
      "#mobile" #> user.mobile.get &
      "#name" #> user.name.get &
      "#gender" #> user.gender &
      "#type" #> user.userType &
      "#email" #> user.email.get &
      "#enabled" #> user.enabled &
      "#isAdmin" #> isSuperUserLabel(user) &
      "#createdAt" #> user.createdAt.asHtml
  }

  def edit = {
    tabMenuRV(Full("edit", "编辑用户"))
    val user = userRV.is.get

    def process(): JsCmd = {
      user.save
      JsRaw(WebHelper.succMsg("opt_profile_tip", Text("信息保存成功！")))
    }

    "@name" #> text(user.name.is, user.name(_)) &
      "@gender" #> selectObj[Genders.Value](Genders.values.toList.map(v => (v, v.toString)), Full(user.gender.is), user.gender(_)) &
      "@user_type" #> selectObj[UserType.Value](UserType.values.toList.map(v => (v, v.toString)), Full(user.userType.is), user.userType(_)) &
      "@enabled" #> selectObj[UserStatus.Value](UserStatus.values.toList.map(v => (v, v.toString)), Full(user.enabled.is), user.enabled(_)) &
      "@qq" #> text(user.qq.is, user.qq(_)) &
      "@phone" #> text(user.phone.is, user.phone(_)) &
      "@email" #> text(user.email.is, user.email(_)) &
      "@address" #> text(user.address.is, user.address(_)) &
      "@sub" #> hidden(process)
  }
}