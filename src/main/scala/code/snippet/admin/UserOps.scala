package code.snippet.admin

import scala.xml.NodeSeq
import scala.xml.Text
import code.lib.BoxConfirm
import code.lib.WebHelper
import code.model.User
import code.model.UserStatus
import code.model.UserSupper
import code.model.UserType
import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.http.RequestVar
import net.liftweb.http.S
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.SHtml.a
import net.liftweb.http.SHtml.ajaxInvoke
import net.liftweb.http.SHtml.hidden
import net.liftweb.http.SHtml.selectObj
import net.liftweb.http.SHtml.text
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds.cmdToString
import net.liftweb.http.js.JsCmds.jsExpToJsCmd
import net.liftweb.mapper.By
import net.liftweb.mapper.Descending
import net.liftweb.mapper.Genders
import net.liftweb.mapper.MaxRows
import net.liftweb.mapper.OrderBy
import net.liftweb.mapper.StartAt
import net.liftweb.util.CssSel
import net.liftweb.util.Helpers.asLong
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import code.snippet.SnippetHelper
import code.snippet.PaginatorHelper

object userRV extends RequestVar[Box[User]](Empty)
object UserOps extends PaginatorHelper[User] with SnippetHelper {

  override def itemsPerPage = 10
  override def count = {
    User.count()
  }
  override def page = User.findAll(StartAt(curPage * itemsPerPage), MaxRows(itemsPerPage), OrderBy(User.createdAt, Descending))

  def list = {
    def actions(user: User): NodeSeq = {
      <a href={ "/admin/user/edit?id=" + user.id.get } class="btn btn-small btn-info"><i class="icon-edit"></i></a> ++ Text(" ") ++
        a(() => {
          BoxConfirm("确定删除【" + user.mobile.get + "】用户？此操作不可恢复，请谨慎！", {
            ajaxInvoke(() => {
              user.delete_!
              JsCmds.Reload
            })._2
          })
        }, Text("删除"), "class" -> "btn btn-danger")
    }

    "tr" #> page.map(user => {
      "#id" #> user.id &
        "#mobile" #> <a href={ "/admin/user/view?id=" + user.id.get }>{ user.mobile.get }</a> &
        "#name" #> user.name.get &
        "#gender" #> user.gender &
        "#type" #> user.userType &
        "#email" #> user.email.get &
        "#enabled" #> user.enabled &
        "#isAdmin" #> user.displaySuper &
        //"#brandCount" #> link("/admin/brand/", () => userRV(Full(user)), Text(user.brandCount.toString), "class" -> "badge badge-success") &
        "#actions" #> actions(user)
    })
  }

  def view = {
    tabMenuRV(Full("zoom-in" -> "查看用户"))
    (for {
      userId <- S.param("id").flatMap(asLong) ?~ "用户ID不存在或无效"
      user <- User.find(By(User.id, userId)) ?~ s"ID为${userId}的用户不存在。"
    } yield {
      "#id" #> user.id &
        "#mobile" #> user.mobile.get &
        "#name" #> user.name.get &
        "#gender" #> user.gender &
        "#type" #> user.userType &
        "#email" #> user.email.get &
        "#enabled" #> user.enabled &
        "#isAdmin" #> user.displaySuper &
        "#createdAt" #> user.createdAt.asHtml &
        "#edit-btn" #> <a href={ "/admin/user/edit?id=" + user.id.get } class="btn btn-primary"><i class="icon-edit"></i> 修改用户信息</a> &
        "#list-btn" #> <a href="/admin/user/" class="btn btn-success"><i class="icon-list"></i> 用户列表</a>
    }): CssSel
  }

  def edit = {
    tabMenuRV(Full("edit" -> "编辑用户"))
    (for {
      userId <- S.param("id").flatMap(asLong) ?~ "用户ID不存在或无效"
      user <- User.find(By(User.id, userId)) ?~ s"ID为${userId}的用户不存在。"
    } yield {
      def process(): JsCmd = {
        user.save
        JsRaw(WebHelper.succMsg("opt_profile_tip", Text("信息保存成功！")))
      }

      val isSupper = if (user.superUser.get) UserSupper.Supper else UserSupper.Normal
      def setSupper(user: User, v: UserSupper.Value) {
        v match {
          case UserSupper.Normal => user.superUser(false)
          case UserSupper.Supper => user.superUser(true)
        }
      }

      "@name" #> text(user.name.is, user.name(_)) &
        "@gender" #> selectObj[Genders.Value](Genders.values.toList.map(v => (v, v.toString)), Full(user.gender.is), user.gender(_)) &
        "@user_type" #> selectObj[UserType.Value](UserType.values.toList.map(v => (v, v.toString)), Full(user.userType.is), user.userType(_)) &
        "@enabled" #> selectObj[UserStatus.Value](UserStatus.values.toList.map(v => (v, v.toString)), Full(user.enabled.is), user.enabled(_)) &
        "@super" #> selectObj[UserSupper.Value](UserSupper.values.toList.map(v => (v, v.toString)), Full(isSupper), setSupper(user, _)) &
        "@qq" #> text(user.qq.is, user.qq(_)) &
        "@phone" #> text(user.phone.is, user.phone(_)) &
        "@email" #> text(user.email.is, user.email(_)) &
        "@address" #> text(user.address.is, user.address(_)) &
        "@sub" #> hidden(process)
    }): CssSel
  }
}