package bootstrap.liftweb

import code.model.MyDBVendor
import code.model.User
import net.liftmodules.FoBo
import net.liftweb.common.Full
import net.liftweb.db.DB
import net.liftweb.db.DB1.db1ToDb
import net.liftweb.db.DefaultConnectionIdentifier
import net.liftweb.http.Html5Properties
import net.liftweb.http.LiftRules
import net.liftweb.http.LiftRulesMocker.toLiftRules
import net.liftweb.http.ParsePath
import net.liftweb.http.Req
import net.liftweb.http.RewriteRequest
import net.liftweb.http.RewriteResponse
import net.liftweb.mapper.Schemifier
import net.liftweb.sitemap.{ ** => ** }
import net.liftweb.sitemap.Loc.Hidden
import net.liftweb.sitemap.Loc.LocGroup
import net.liftweb.sitemap.LocPath.stringToLocPath
import net.liftweb.sitemap.Menu
import net.liftweb.sitemap.SiteMap
import net.liftweb.util.Vendor.valToVender
import net.liftweb.sitemap.Loc
import code.model.Brand
import net.liftweb.http.RedirectResponse
import net.liftweb.http.S
import code.config.Site

class Boot {
  def boot {
    LiftRules.addToPackages("code")
    DB.defineConnectionManager(DefaultConnectionIdentifier, MyDBVendor)
    Schemifier.schemify(true, Schemifier.infoF _, User, Brand)

    FoBo.InitParam.JQuery = FoBo.JQuery191
    FoBo.InitParam.ToolKit = FoBo.Bootstrap231
    FoBo.InitParam.ToolKit = FoBo.FontAwesome300
    FoBo.init()

    LiftRules.setSiteMap(Site.siteMap)

    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)
    LiftRules.early.append(_.setCharacterEncoding("utf-8"))
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    LiftRules.loggedInTest = Full(
      () => {
        User.loggedIn_?
      })

    //LiftRules.setSiteMapFunc(() => User.sitemapMutator(MenuInfo.sitemap))

    //Rewrite
    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath("user" :: "sign_out" :: Nil, _, _, _), _, _) =>
        RewriteResponse("user_mgt" :: "logout" :: Nil)
    }
  }
}

object MenuInfo {
  import Loc._
  import scala.xml._

  val IfUserLoggedIn = If(() => User.loggedIn_?, () => RedirectResponse("/"))
  val HiddenSign = Unless(() => User.loggedIn_?, () => RedirectResponse("/user/"))

  val menus = List(
    Menu("Home", S.loc("home", <span><i class="icon-home"></i> 首页</span>)) / "index" >> LocGroup("main"),
    Menu("Project", S.loc("project", <span><i class="icon-file-alt"></i> 项目</span>)) / "project" / ** >> LocGroup("main"),
    Menu("Wenda", S.loc("wenda", <span><i class="icon-question-sign"></i> 问答</span>)) / "wenda" / ** >> LocGroup("main"),
    Menu("Story", S.loc("story", <span><i class="icon-book"></i> 日志</span>)) / "story" / ** >> LocGroup("main"),
    Menu("Album", S.loc("album", <span><i class="icon-folder-open-alt"></i> 专辑</span>)) / "album" / ** >> LocGroup("main"),
    Menu("Resource", S.loc("resource", <span><i class="icon-table"></i> 资源</span>)) / "resource" / ** >> LocGroup("main"),
    Menu("用户后台") / "user" / ** >> IfUserLoggedIn >> LocGroup("user"))

  /*Menu("首页") / "index" >> LocGroup("main"),
    Menu("商标集市") / "market" / ** >> LocGroup("main"),
    Menu("商标查询") / "brand-search" / ** >> LocGroup("main"),
    Menu("问答频道") / "wenda" / ** >> LocGroup("main"),
    Menu("用户后台") / "user" / ** >> IfUserLoggedIn >> LocGroup("user"))*/

  def sitemap() = SiteMap(menus: _*)
}
