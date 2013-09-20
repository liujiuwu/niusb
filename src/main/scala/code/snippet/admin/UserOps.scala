package code.snippet.admin

import scala.xml.NodeSeq
import scala.xml.Text

import code.model.User
import code.model.UserStatus
import code.model.UserSupper
import code.model.UserType
import code.snippet.SnippetHelper
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds.jsExpToJsCmd
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.util.Helpers
import net.liftweb.util.Helpers._
import com.niusb.util.WebHelpers._

object UserOps extends DispatchSnippet with SnippetHelper with Loggable {

  def dispatch = {
    case "list" => list
    case "view" => view
    case "edit" => edit
  }

  private def bies: List[QueryParam[User]] = {
    val (searchType, keyword) = (S.param("type"), S.param("keyword"))
    var byList = List[QueryParam[User]](OrderBy(User.createdAt, Descending))
    keyword match {
      case Full(k) if (!k.trim().isEmpty()) =>
        val kv = k.trim()
        searchType match {
          case Full("0") => byList = By(User.id, kv.toLong) :: byList
          case Full("1") => byList = By(User.mobile, kv) :: byList
          case _ =>
        }
      case _ =>
    }
    byList
  }

  def list = {
    def actions(user: User): NodeSeq = {
      <a href={ "/admin/user/edit?id=" + user.id.get } class="btn btn-info"><i class="icon-edit"></i></a> ++ Text(" ") ++
        a(() => {
          BoxConfirm("确定删除【" + user.mobile.get + "】用户？此操作不可恢复，请谨慎！", {
            ajaxInvoke(() => {
              user.delete_!
              JsCmds.Reload
            })._2
          })
        }, <i class="icon-trash"></i>, "class" -> "btn btn-danger")
    }

    val (searchType, keyword) = (S.param("type"), S.param("keyword"))
    var searchTypeVal, keywordVal = ""
    var url = "/admin/user/"
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
    val paginatorModel = User.paginator(url, bies: _*)()

    val searchForm = "#searchForm" #>
      <form class="form-inline" action={ url } method="get">
        <div class="form-group">
          <select class="form-control" id="searchType" name="type" style="width:120px">
            <option value="0" selected={ if (searchTypeVal == "0") "selected" else null }>用户ID</option>
            <option value="1" selected={ if (searchTypeVal == "1") "selected" else null }>手机号</option>
          </select>
        </div>
        <div class="form-group">
          <input type="text" class="form-control" id="keyword" name="keyword" value={ keywordVal } style="width:250px"/>
        </div>
        <button type="submit" class="btn btn-primary"><i class="icon-search"></i> 搜索</button>
      </form>

    val dataList = "#dataList tr" #> paginatorModel.datas.map(user => {
      "#id" #> user.id &
        "#mobile" #> <a href={ "/admin/user/view?id=" + user.id.get }>{ user.mobile.get }</a> &
        "#name" #> user.name.get &
        "#gender" #> user.gender &
        "#type" #> user.userType &
        "#email" #> user.email.get &
        "#enabled" #> user.enabled &
        "#isAdmin" #> user.displaySuper &
        "#actions" #> actions(user)
    })

    searchForm & dataList & "#pagination" #> paginatorModel.paginate _
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
        "#upgradedAt" #> user.upgradedAt.asHtml &
        "#email" #> user.email.get &
        "#phone" #> user.phone.get &
        "#qq" #> user.qq.get &
        "#address" #> user.address.get &
        "#enabled" #> user.enabled &
        "#isAdmin" #> user.displaySuper &
        "#createdAt" #> user.createdAt.asHtml &
        "#edit-btn" #> <a href={ "/admin/user/edit?id=" + user.id.get } class="btn btn-primary"><i class="icon-edit"></i> 修改用户信息</a> &
        "#list-btn" #> <a href="/admin/user/" class="btn btn-success"><i class="icon-list"></i> 用户列表</a>
    }): CssSel
  }

  def edit = {
    tabMenuRV(Full("edit" -> "编辑用户"))
    var userType = UserType.Normal
    (for {
      userId <- S.param("id").flatMap(asLong) ?~ "用户ID不存在或无效"
      user <- User.find(By(User.id, userId)) ?~ s"ID为${userId}的用户不存在。"
    } yield {
      userType = user.userType.get
      def process(): JsCmd = {
        if (userType != user.userType) {
          user.upgradedAt(Helpers.now)
        }
        user.save
        //JsRaw(WebHelper.succMsg("opt_profile_tip", Text("信息保存成功！")))
        S.redirectTo("/admin/user/view?id=" + user.id.get)
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
        "#view-btn" #> <a href={ "/admin/user/view?id=" + user.id.get } class="btn btn-primary"><i class="icon-info"></i> 查看用户</a> &
        "#list-btn" #> <a href="/admin/user/" class="btn btn-success"><i class="icon-list"></i> 用户列表</a> &
        "@sub" #> hidden(process)
    }): CssSel
  }
}