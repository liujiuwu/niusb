package code
package config

import net.liftweb._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.http.S

object MenuGroups {
  val TopBarGroup = LocGroup("topbar")
}

object Site {
  import MenuGroups._

  private def menus = List(
    Menu("Home", S.loc("home", <span><i class="icon-home"></i> 首页</span>)) / "index" >> TopBarGroup,
    Menu("Market", S.loc("project", <span><i class="icon-globe"></i> 商标集市</span>)) / "market" / ** >> TopBarGroup,
    Menu("Wenda", S.loc("wenda", <span><i class="icon-question-sign"></i> 问答频道</span>)) / "wenda" / ** >> TopBarGroup,
    Menu ("BrandSearch", S.loc("BrandSearch", <span><i class="icon-search"></i> 商标查询</span>)) / "brand-search" >> TopBarGroup,
    Menu.i("Error") / "error" >> Hidden,
    Menu.i("404") / "404" >> Hidden,
    Menu.i("Throw") / "throw" >> Hidden >> EarlyResponse(() => throw new Exception("This is only a test.")))

  /*
* Return a SiteMap needed for Lift
*/
  def siteMap: SiteMap = SiteMap(menus: _*)
}