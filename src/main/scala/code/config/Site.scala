package code
package config

import scala.xml.Text
import code.model.User
import net.liftweb._
import net.liftweb.http.Factory
import net.liftweb.http.RedirectResponse
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.sitemap.Loc
import scala.xml.NodeSeq
import net.liftweb.http.Templates

object MenuGroups {
  val TopBarGroup = LocGroup("topbar")
  val UserMenuGroup = LocGroup("userMenu")
  val AdminMenuGroup = LocGroup("adminMenu")
  val AboutGroup = LocGroup("aboutMenu")
}

object Site {
  import MenuGroups._

  val RequireLoggedIn = If(() => User.loggedIn_?, () => RedirectResponse("/"))
  val RequireAdminLoggedIn = If(() => User.loggedIn_? && User.superUser_?, () => RedirectResponse("/"))
  val HiddenSign = Unless(() => User.loggedIn_?, () => RedirectResponse("/user/"))

  val pageMenus = List[Menu](
    Menu(Loc("Home", List("index"), menuText("首页", "home"), TopBarGroup)),
    Menu(Loc("Market", List("market", "index"), menuText("商标集市", "globe"), TopBarGroup)),
    Menu(Loc("ViewBrand", List("market", "view"), menuText("查看商标"), Hidden)),
    Menu(Loc("Recommend", List("market", "recommend"), menuText("精品商标", "sun"), TopBarGroup)),
    Menu(Loc("Offer", List("market", "offer"), menuText("特价商标", "tag"), TopBarGroup)),
    Menu(Loc("Own", List("market", "own"), menuText("自有商标", "asterisk"), TopBarGroup)),
    Menu(Loc("News", List("news", "index"), menuText("新闻公告"), Hidden)),
    Menu(Loc("ViewNews", List("news", "view"), menuText("查看新闻"), Hidden)),
    Menu(Loc("Wenda", List("wenda") -> true, menuText("问答频道", "question-sign"), TopBarGroup)))

  val userMenus = List[Menu](
    Menu(Loc("Profile", List("user", "profile"), menuText("帐户信息", "edit"), RequireLoggedIn, UserMenuGroup)),
    Menu(Loc("Pwd", List("user", "pwd"), menuText("修改密码", "key"), RequireLoggedIn, UserMenuGroup)),
    Menu(Loc("UserCreateBrand", List("user", "brand", "create"), menuText("转让商标", "plus"), RequireLoggedIn, UserMenuGroup)),
    Menu(Loc("UserListBrand", List("user", "brand", "index"), menuText("我的商标", "list"), RequireLoggedIn, UserMenuGroup)),
    Menu(Loc("UserViewBrand", List("user", "brand", "view"), menuText("查看商标"), RequireLoggedIn, Hidden)),
    Menu(Loc("UserEditBrand", List("user", "brand", "edit"), menuText("修改商标"), RequireLoggedIn, Hidden)),
    Menu(Loc("UserFollow", List("user", "brand", "follow"), menuText("我的关注", "heart"), RequireLoggedIn, UserMenuGroup)),
    Menu(Loc("UserSms", List("user", "sms", "index"), menuText("我的消息", "envelope"), RequireLoggedIn, UserMenuGroup)),
    Menu(Loc("UserViewSms", List("user", "sms", "view"), menuText("查看消息"), RequireLoggedIn, Hidden)))

  val adminMenus = List[Menu](
    Menu(Loc("AdminIndex", List("admin", "index"), menuText("后台首页", "home"), RequireAdminLoggedIn, AdminMenuGroup)),
    Menu(Loc("AdminSetting", List("admin", "web", "set"), menuText("网站设置", "cogs"), RequireAdminLoggedIn, AdminMenuGroup)),
    Menu(Loc("AdminListUser", List("admin", "user", "index"), menuText("用户管理", "user-md"), RequireAdminLoggedIn, AdminMenuGroup)),
    Menu(Loc("AdminViewUser", List("admin", "user", "view"), menuText("查看用户"), RequireAdminLoggedIn, Hidden)),
    Menu(Loc("AdminEditUser", List("admin", "user", "edit"), menuText("修改用户"), RequireAdminLoggedIn, Hidden)),

    Menu(Loc("AdminListBrand", List("admin", "brand", "index"), menuText("商标管理", "list"), RequireAdminLoggedIn, AdminMenuGroup)),
    Menu(Loc("AdminViewBrand", List("admin", "brand", "view"), menuText("查看商标"), RequireAdminLoggedIn, Hidden)),
    Menu(Loc("AdminEditBrand", List("admin", "brand", "edit"), menuText("修改商标"), RequireAdminLoggedIn, Hidden)),
    Menu(Loc("AdminSeditBrand", List("admin", "brand", "sedit"), menuText("商标设置"), RequireAdminLoggedIn, Hidden)),
    Menu(Loc("AdminListArticle", List("admin", "article", "index"), menuText("文章管理", "list"), RequireAdminLoggedIn, AdminMenuGroup)),
    Menu(Loc("AdminCreateArticle", List("admin", "article", "create"), menuText("发布文章", "plus"), RequireAdminLoggedIn, Hidden)),
    Menu(Loc("AdminEditArticle", List("admin", "article", "edit"), menuText("修改文章"), RequireAdminLoggedIn, Hidden)),
    Menu(Loc("AdminListWenda", List("admin", "wenda", "index"), menuText("问答管理", "list"), RequireAdminLoggedIn, AdminMenuGroup)),
    Menu(Loc("AdminCreateWenda", List("admin", "wenda", "create"), menuText("发布问答", "plus"), RequireAdminLoggedIn, Hidden)),
    Menu(Loc("AdminListSms", List("admin", "sms", "index"), menuText("消息管理", "envelope"), RequireAdminLoggedIn, AdminMenuGroup)),
    Menu(Loc("AdminCreateSms", List("admin", "sms", "create"), menuText("发送消息", "plus"), RequireAdminLoggedIn, Hidden)))

  val aboutMenus = List[Menu](
    Menu(Loc("About", List("help", "about"), menuText("关于我们"), AboutGroup)),
    Menu(Loc("ContactUs", List("help", "contact_us"), menuText("联系我们"), AboutGroup)),
    Menu(Loc("PayInfo", List("help", "pay_info"), menuText("付款账户"), AboutGroup)),
    Menu(Loc("Sitemap", List("help", "sitemap"), menuText("网站地图"), AboutGroup)))

  val otherMenus = List(
    Menu.i("ajaxExample") / "ajax",
    Menu.i("Error") / "error" >> Hidden,
    Menu.i("404") / "404" >> Hidden,
    /* Menu.i("Help") / "help" / ** >> Hidden,*/
    Menu.i("Throw") / "throw" >> Hidden >> EarlyResponse(() => throw new Exception("This is only a test.")))

  val menus = pageMenus ::: userMenus ::: adminMenus ::: aboutMenus ::: otherMenus

  val m = Menu(Loc("AdminCreateSms", List("tt"), "", RequireAdminLoggedIn, Hidden), otherMenus: _*)
  def menuText(lnText: String, icon: String = ""): NodeSeq = {
    val iconNodeSeq = if (!icon.isEmpty) <i class={ "icon-" + icon }></i> ++ Text(" ") else Text("")
    <span>{ iconNodeSeq ++ lnText }</span>
  }

  def siteMap: SiteMap = User.sitemapMutator(SiteMap(menus: _*))
}