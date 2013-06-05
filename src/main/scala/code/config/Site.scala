package code
package config

import net.liftweb._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.http.S
import code.model.User
import net.liftweb.http.RedirectResponse

object MenuGroups {
  val TopBarGroup = LocGroup("topbar")
  val UserMenuGroup = LocGroup("userMenu")
  val AdminMenuGroup = LocGroup("adminMenu")
}

object Site {
  import MenuGroups._

  val IfUserLoggedIn = If(() => User.loggedIn_?, () => RedirectResponse("/"))
  val HiddenSign = Unless(() => User.loggedIn_?, () => RedirectResponse("/user/"))

  private def menus = List(
    Menu("Home", S.loc("home", <span><i class="icon-home"></i> 首页</span>)) / "index" >> TopBarGroup,
    Menu("Market", S.loc("project", <span><i class="icon-globe"></i> 商标集市</span>)) / "market" / ** >> TopBarGroup,
    Menu("Wenda", S.loc("wenda", <span><i class="icon-question-sign"></i> 问答频道</span>)) / "wenda" / ** >> TopBarGroup,
    Menu("BrandSearch", S.loc("BrandSearch", <span><i class="icon-search"></i> 商标查询</span>)) / "brand-search" >> TopBarGroup,
    Menu("Profile", S.loc("Profile", <span><i class="icon-edit"></i> 帐户信息</span>)) / "user" / "profile" >> IfUserLoggedIn >> UserMenuGroup,
    Menu("AddBrand", S.loc("AddBrand", <span><i class="icon-plus"></i> 发布商标</span>)) / "user" / "brand" / "add" >> IfUserLoggedIn >> UserMenuGroup,
    Menu("MyBrands", S.loc("MyBrands", <span><i class="icon-list"></i> 我的商标</span>)) / "user" / "brand" / "index" >> IfUserLoggedIn >> UserMenuGroup,
    Menu("AdminUser", S.loc("AdminUser", <span><i class="icon-list"></i> 用户管理</span>)) / "admin" / "user" / ** >> IfUserLoggedIn >> AdminMenuGroup,
    Menu.i("ajaxExample") / "ajax",
    Menu.i("Error") / "error" >> Hidden,
    Menu.i("404") / "404" >> Hidden,
    Menu.i("Throw") / "throw" >> Hidden >> EarlyResponse(() => throw new Exception("This is only a test.")))

  /*
* Return a SiteMap needed for Lift
*/
  def siteMap: SiteMap = User.sitemapMutator(SiteMap(menus: _*))
}