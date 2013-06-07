package code
package config

import net.liftweb._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.http.S
import code.model.User
import net.liftweb.http.RedirectResponse
import net.liftweb.http.Factory

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
    Menu("AddBrand", menuLoc("AddBrand", "plus", "发布商标")) / "user" / "brand" / "add" >> RequireLoggedIn >> UserMenuGroup,
    Menu("MyBrands", menuLoc("MyBrands", "list", "我的商标")) / "user" / "brand" / "index" >> RequireLoggedIn >> UserMenuGroup,
    Menu("AdminUserList", menuLoc("AdminUserList", "user-md", "用户管理")) / "admin" / "user" / ** >> RequireAdminLoggedIn >> AdminMenuGroup,
    Menu("AdminBrandList", menuLoc("AdminBrandList", "list", "商标管理")) / "admin" / "brand" / ** >> RequireAdminLoggedIn >> AdminMenuGroup,
    Menu.i("ajaxExample") / "ajax",
    Menu.i("Error") / "error" >> Hidden,
    Menu.i("404") / "404" >> Hidden,
    Menu.i("Throw") / "throw" >> Hidden >> EarlyResponse(() => throw new Exception("This is only a test.")))

  def menuLoc(name: String, icon: String, linkText: String) = {
    S.loc(name, <span><i class={ "icon-" + icon }></i> { linkText }</span>)
  }
  def siteMap: SiteMap = User.sitemapMutator(SiteMap(menus: _*))
}