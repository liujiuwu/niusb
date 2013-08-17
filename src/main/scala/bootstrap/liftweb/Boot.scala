package bootstrap.liftweb

import java.util.Date
import code.config.Site
import code.lib.SearchHelper
import code.model.MyDBVendor
import code.model.User
import code.rest.UploadManager
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.db.DB
import net.liftweb.db.DB1.db1ToDb
import net.liftweb.db.DefaultConnectionIdentifier
import net.liftweb.http.Html5Properties
import net.liftweb.http.LiftRules
import net.liftweb.http.LiftRulesMocker.toLiftRules
import net.liftweb.http.NotFoundAsTemplate
import net.liftweb.http.ParsePath
import net.liftweb.http.Req
import net.liftweb.http.RewriteRequest
import net.liftweb.http.RewriteResponse
import net.liftweb.http.S
import net.liftweb.mapper.By
import net.liftweb.util.NamedPF
import net.liftweb.util.Props
import net.liftweb.util.Vendor.valToVender
import net.liftweb.http.provider.HTTPCookie
import net.liftweb.mapper.Schemifier
import code.model.Brand
import scala.collection.Parallel
import code.lib.SyncData
import code.model.BrandType
import code.model.AdSpace
import code.model.Ad
import code.model.Article
import code.model.Message
import code.lib.WebCacheHelper

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
    //DB.addLogFunc((query, len) => logger.info("The query: " + query + " took " + len + " milliseconds"))
    Schemifier.schemify(true, Schemifier.infoF _, User, BrandType, Brand, AdSpace, Ad, Article, Message)

    LiftRules.setSiteMap(Site.siteMap)

    LiftRules.maxMimeFileSize = 40000000L
    LiftRules.maxMimeSize = 40000000L
    LiftRules.dispatch.append(UploadManager)

    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)
    LiftRules.early.append(_.setCharacterEncoding("utf-8"))
    //LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))
    LiftRules.htmlProperties.default.set((r: Req) => new StarXHtmlInHtml5OutProperties(r.userAgent))
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
      case Req("initData" :: Nil, _, _) =>
        SearchHelper.initData
    }

    LiftRules.uriNotFound.prepend(NamedPF("404handler") {
      case (req, failure) =>
        NotFoundAsTemplate(ParsePath(List("404"), "html", true, false))
    })

    /*LiftRules.snippetDispatch.append{
      case "AdminBrandOps" => code.snippet.admin.BrandOps
      case "UserBrandOps" => code.snippet.user.BrandOps
    }*/

    /*LiftRules.exceptionHandler.prepend {
      case (runMode, req, exception) =>
        logger.error("Failed at: " + req.uri, exception)
        val content = S.render(<lift:embed what="500"/>, req.request)
        XmlResponse(content.head, 500, "text/html", req.cookies)
    }*/

    def testUserLogin() {
      val testUser = User.find(By(User.mobile, "13826526941"))
      testUser foreach { user =>
        User.logUserIn(user)
        /*val random = scala.util.Random.nextLong
        val now = new Date().getTime
        val cookieData = user.mobile.get + ":" + (now ^ random).toString
        val cookie = HTTPCookie("RememberMe", cookieData).setMaxAge(((30000) / 1000L).toInt).setPath("/")
        S.addCookie(cookie)
        logger.info(S.cookieValue("RememberMe"))*/
      }
    }

    User.autologinFunc = if (Props.devMode) Full(testUserLogin) else Empty

    WebCacheHelper.load()
  }
}
