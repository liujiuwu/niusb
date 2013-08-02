package code
package config

import net.liftweb._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.http.S
import code.model.User
import net.liftweb.http.RedirectResponse
import net.liftweb.http.Factory
import scala.xml.Text

object MenuGroups {
  val TopBarGroup = LocGroup("topbar")
  val UserMenuGroup = LocGroup("userMenu")
  val AdminMenuGroup = LocGroup("adminMenu")
}

object Site {
  import MenuGroups._

  val RequireLoggedIn = If(() => User.loggedIn_?, () => RedirectResponse("/"))
  val RequireAdminLoggedIn = If(() => User.loggedIn_? && User.superUser_?, () => RedirectResponse("/"))
  val HiddenSign = Unless(() => User.loggedIn_?, () => RedirectResponse("/user/"))

  private def menus = List(
    Menu("Home", menuLoc("Home", "home", "首页")) / "index" >> TopBarGroup,
    Menu("Market", menuLoc("Market", "globe", "商标集市")) / "market" / ** >> TopBarGroup,
    Menu("Wenda", menuLoc("Wenda", "question-sign", "问答频道")) / "wenda" / ** >> TopBarGroup,
    Menu("BrandSearch", menuLoc("BrandSearch", "search", "商标查询")) / "brand-search" >> TopBarGroup,
    Menu("Profile", menuLoc("Profile", "edit", "帐户信息")) / "user" / "profile" >> RequireLoggedIn >> UserMenuGroup,
    Menu("Pwd", menuLoc("Pwd", "key", "修改密码")) / "user" / "pwd" >> RequireLoggedIn >> UserMenuGroup,
    
    Menu("UserCreateBrand", menuLoc("UserCreateBrand", "plus", "发布商标")) / "user" / "brand" / "create" >> RequireLoggedIn >> UserMenuGroup,
    Menu("UserListBrand", menuLoc("UserListBrand", "list", "我的商标")) / "user" / "brand" / "index" >> RequireLoggedIn >> UserMenuGroup,
    Menu("UserViewBrand", menuLoc("UserViewBrand", "", "查看商标")) / "user" / "brand" / "view" >> RequireLoggedIn >> Hidden,
    Menu("UserEditBrand", menuLoc("UserEditBrand", "", "修改商标")) / "user" / "brand" / "edit" >> RequireLoggedIn >> Hidden,
    
    Menu("AdminIndex", menuLoc("AdminIndex", "home", "后台首页")) / "admin" / "index"  >> RequireAdminLoggedIn >> AdminMenuGroup,
    Menu("AdminSetting", menuLoc("AdminSetting", "cogs", "网站设置")) / "admin" / "web" / "set" >> RequireAdminLoggedIn >> AdminMenuGroup,
    Menu("AdminListUser", menuLoc("AdminListUser", "user-md", "用户管理")) / "admin" / "user" / "index" >> RequireAdminLoggedIn >> AdminMenuGroup,
    Menu("AdminViewUser", menuLoc("AdminViewUser", "", "查看用户")) / "admin" / "user" / "view" >> RequireAdminLoggedIn >> Hidden,
    Menu("AdminEditUser", menuLoc("AdminEditUser", "", "修改用户")) / "admin" / "user" / "edit" >> RequireAdminLoggedIn >> Hidden,
    
    Menu("AdminListBrand", menuLoc("AdminListBrand", "list", "商标管理")) / "admin" / "brand" / "index" >> RequireAdminLoggedIn >> AdminMenuGroup,
    Menu("AdminViewBrand", menuLoc("AdminViewBrand", "", "查看商标")) / "admin" / "brand" / "view" >> RequireAdminLoggedIn >> Hidden,
    Menu("AdminEditBrand", menuLoc("AdminEditBrand", "", "修改商标")) / "admin" / "brand" / "edit" >> RequireAdminLoggedIn >> Hidden,
    Menu("AdminSeditBrand", menuLoc("AdminSeditBrand", "", "商标设置")) / "admin" / "brand" / "sedit" >> RequireAdminLoggedIn >> Hidden,

    
    Menu.i("ajaxExample") / "ajax",
    Menu.i("Error") / "error" >> Hidden,
    Menu.i("404") / "404" >> Hidden,
    Menu.i("Throw") / "throw" >> Hidden >> EarlyResponse(() => throw new Exception("This is only a test.")))

  def menuLoc(name: String, icon: String, linkText: String) = {
    S.loc(name, <span>
                  {
                    if (!icon.isEmpty)
                      <i class={ "icon-" + icon }></i> ++ Text(" ")
                  }{ linkText }
                </span>)
  }

  def siteMap: SiteMap = User.sitemapMutator(SiteMap(menus: _*))
}