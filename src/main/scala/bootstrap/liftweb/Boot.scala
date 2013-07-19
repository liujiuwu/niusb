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
import code.lib.SearchHelper
import net.liftweb.common.Loggable
import net.liftweb.db.DBLogEntry
import net.liftweb.http.OnDiskFileParamHolder
import code.rest.UploadManager
import net.liftweb.util.NamedPF
import net.liftweb.http.NotFoundAsTemplate
import net.liftweb.http.InternalServerErrorResponse
import net.liftweb.http.XmlResponse

class Boot extends Loggable {
  def boot {
    LiftRules.addToPackages("code")
    DB.defineConnectionManager(DefaultConnectionIdentifier, MyDBVendor)
    /*DB.addLogFunc {
      case (log, duration) => {
        logger.debug("Total query time : %d ms".format(duration))
        log.allEntries.foreach {
          case DBLogEntry(stmt, duration) =>
            logger.debug("  %s in %d ms".format(stmt, duration))
        }
      }
    }*/
    DB.addLogFunc((query, len) => logger.info("The query: " + query + " took " + len + " milliseconds"))
    Schemifier.schemify(true, Schemifier.infoF _, User, Brand)

    /*FoBo.InitParam.JQuery = FoBo.JQuery191
    FoBo.InitParam.ToolKit = FoBo.Bootstrap231
    FoBo.InitParam.ToolKit = FoBo.FontAwesome300
    FoBo.init()*/

    LiftRules.setSiteMap(Site.siteMap)

    LiftRules.maxMimeFileSize = 40000000L
    LiftRules.maxMimeSize = 40000000L
    LiftRules.dispatch.append(UploadManager)

    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)
    LiftRules.early.append(_.setCharacterEncoding("utf-8"))
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))
    //LiftRules.handleMimeFile = OnDiskFileParamHolder.apply

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

    LiftRules.dispatch.append {
      case Req("brand" :: regNo :: Nil, _, _) =>
        SearchHelper.searchBrandPicByRegNo(regNo)
    }

    LiftRules.uriNotFound.prepend(NamedPF("404handler") {
      case (req, failure) =>
        NotFoundAsTemplate(ParsePath(List("404"), "html", true, false))
    })

    LiftRules.exceptionHandler.prepend {
      case (runMode, req, exception) =>
        logger.error("Failed at: " + req.uri, exception)
        val content = S.render(<lift:embed what="500"/>, req.request)
        XmlResponse(content.head, 500, "text/html", req.cookies)
    }
  }
}
