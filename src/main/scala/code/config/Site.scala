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
    Menu("用户后台") / "user" / ** >> IfUserLoggedIn >> UserMenuGroup,
    Menu.i("Error") / "error" >> Hidden,
    Menu.i("404") / "404" >> Hidden,
    Menu.i("Throw") / "throw" >> Hidden >> EarlyResponse(() => throw new Exception("This is only a test.")))

  /*
* Return a SiteMap needed for Lift
*/
  def siteMap: SiteMap = User.sitemapMutator(SiteMap(menus: _*))
}