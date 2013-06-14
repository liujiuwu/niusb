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

object UserOps extends TabMenu with MyPaginatorSnippet[User] {
  object userRV extends RequestVar[Box[User]](Empty)

  override def itemsPerPage = 10
  override def count = User.count()
  override def page = User.findAll(StartAt(curPage * itemsPerPage), MaxRows(itemsPerPage), OrderBy(User.createdAt, Descending))

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
        "#enabled" #> { if (user.enabled.get) <span class="badge badge-success">正常</span> else <span class="badge badge-important">禁止</span> } &
        "#isAdmin" #> { if (user.superUser.get) <span class="badge badge-success">是</span> else <span class="badge badge-important">否</span> } &
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
      "#enabled" #> { if (user.enabled.get) <span class="badge badge-success">正常</span> else <span class="badge badge-important">禁止</span> } &
      "#isAdmin" #> { if (user.superUser.get) <span class="badge badge-success">是</span> else <span class="badge badge-important">否</span> } &
      "#createdAt" #> user.createdAt.asHtml
  }
}