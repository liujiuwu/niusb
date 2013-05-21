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
import net.liftweb.sitemap.{** => **}
import net.liftweb.sitemap.Loc.Hidden
import net.liftweb.sitemap.Loc.LocGroup
import net.liftweb.sitemap.LocPath.stringToLocPath
import net.liftweb.sitemap.Menu
import net.liftweb.sitemap.SiteMap
import net.liftweb.util.Vendor.valToVender
import net.liftweb.sitemap.Loc

class Boot {
  def boot {
    LiftRules.addToPackages("code")
    DB.defineConnectionManager(DefaultConnectionIdentifier, MyDBVendor)
    Schemifier.schemify(true, Schemifier.infoF _, User)

    FoBo.InitParam.JQuery = FoBo.JQuery191
    FoBo.InitParam.ToolKit = FoBo.Bootstrap231
    FoBo.InitParam.ToolKit = FoBo.FontAwesome300
    FoBo.init()

    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)
    LiftRules.early.append(_.setCharacterEncoding("utf-8"))
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    LiftRules.setSiteMapFunc(() => User.sitemapMutator(MenuInfo.sitemap))

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

  val menus = List(
    Menu("首页") / "index" >> LocGroup("main"),
    Menu("商标集市") / "meinv" / ** >> LocGroup("main"),
    Menu("商标查询/注册") / "shuaige" / ** >> LocGroup("main"),
    Menu("问答频道") / "keai" / ** >> LocGroup("main"))

  def sitemap() = SiteMap(menus: _*)
}
