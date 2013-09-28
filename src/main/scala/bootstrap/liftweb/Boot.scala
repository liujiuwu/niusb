package bootstrap.liftweb

import code.config.Site
import code.lib.WebCacheHelper
import code.model._
import code.rest.UploadManager
import net.liftweb.common._
import net.liftweb.db.DB
import net.liftweb.db.DB1.db1ToDb
import net.liftweb.db.DefaultConnectionIdentifier
import net.liftweb.http.LiftRules
import net.liftweb.http.LiftRulesMocker.toLiftRules
import net.liftweb.http.NotFoundAsTemplate
import net.liftweb.http.NoticeType
import net.liftweb.http.ParsePath
import net.liftweb.http.Req
import net.liftweb.http.RewriteRequest
import net.liftweb.http.RewriteResponse
import net.liftweb.http.js.JE
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper._
import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb.util.Vendor.valToVender

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
    Schemifier.schemify(true, Schemifier.infoF _, User, Webset, BrandType, Brand, AdSpace, Ad, Article, Message, UserData, WendaType, Wenda, WendaReply, BrandApplication)

    LiftRules.setSiteMap(Site.siteMap)

    LiftRules.maxMimeFileSize = 40000000L
    LiftRules.maxMimeSize = 40000000L
    LiftRules.dispatch.append(UploadManager)

    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)
    LiftRules.early.append(_.setCharacterEncoding("utf-8"))
    LiftRules.noticesAutoFadeOut.default.set((noticeType: NoticeType.Value) => Full((1 seconds, 2 seconds)))
    //LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))
    LiftRules.htmlProperties.default.set((r: Req) => new StarXHtmlInHtml5OutProperties(r.userAgent))
    //LiftRules.handleMimeFile = OnDiskFileParamHolder.apply

    val jsNotice =
      """$('#lift__noticesContainer___notice')
        |.addClass("alert alert-success")
        |.prepend('<button type="button" class="close" data-dismiss="alert">×</button>')""".stripMargin

    val jsWarning =
      """$('#lift__noticesContainer___warning')
        |.addClass("alert alert-warning")
        |.prepend('<button type="button" class="close" data-dismiss="alert">×</button>')""".stripMargin

    val jsError =
      """$('#lift__noticesContainer___error')
        |.addClass("alert alert-danger")
        |.prepend('<button type="button" class="close" data-dismiss="alert">×</button>')""".stripMargin

    LiftRules.noticesEffects.default.set(
      (notice: Box[NoticeType.Value], id: String) => {
        val js = notice.map(_.title) match {
          case Full("Notice") => Full(JE.JsRaw(jsNotice).cmd)
          case Full("Warning") => Full(JE.JsRaw(jsWarning).cmd)
          case Full("Error") => Full(JE.JsRaw(jsError).cmd)
          case _ => Full(Noop) //Full(JE.JsRaw( jsNotice ).cmd)
        }
        js
      })

    LiftRules.loggedInTest = Full(
      () => {
        User.loggedIn_?
      })

    //LiftRules.setSiteMapFunc(() => User.sitemapMutator(MenuInfo.sitemap))

    //Rewrite
    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath("/" :: Nil, _, _, _), _, _) =>
        RewriteResponse("index" :: Nil)
      case RewriteRequest(ParsePath("market" :: Nil, _, _, _), _, _) =>
        RewriteResponse("market" :: "index" :: Nil, Map("pageType" -> "0", "brandTypeCode" -> "0", "orderType" -> "0"))
      case RewriteRequest(ParsePath("market" :: AsInt(pageType) :: AsInt(brandTypeCode) :: AsInt(orderType) :: Nil, _, _, _), _, _) =>
        RewriteResponse("market" :: "index" :: Nil, Map("pageType" -> pageType.toString, "brandTypeCode" -> brandTypeCode.toString, "orderType" -> orderType.toString))
      case RewriteRequest(ParsePath("brand" :: Nil, _, _, _), _, _) =>
        RewriteResponse("brand" :: "index" :: Nil)

      case RewriteRequest(ParsePath("user" :: "sign_out" :: Nil, _, _, _), _, _) =>
        RewriteResponse("user_mgt" :: "logout" :: Nil)
      case RewriteRequest(ParsePath("market" :: AsLong(id) :: Nil, _, _, _), _, _) =>
        RewriteResponse("market" :: "view" :: Nil, Map("id" -> id.toString))

      case RewriteRequest(ParsePath("user" :: "brand" :: AsLong(id) :: Nil, _, _, _), _, _) =>
        RewriteResponse("user" :: "brand" :: "view" :: Nil, Map("id" -> id.toString))

      case RewriteRequest(ParsePath("news" :: Nil, _, _, _), _, _) =>
        RewriteResponse("news" :: "index" :: Nil)
      case RewriteRequest(ParsePath("news" :: "view" :: AsLong(id) :: Nil, _, _, _), _, _) =>
        RewriteResponse("news" :: "view" :: Nil, Map("id" -> id.toString))

      case RewriteRequest(ParsePath("help" :: Nil, _, _, _), _, _) =>
        RewriteResponse("help" :: "index" :: Nil)
      case RewriteRequest(ParsePath("about" :: Nil, _, _, _), _, _) =>
        RewriteResponse("help" :: "about" :: Nil)
      case RewriteRequest(ParsePath("contact_us" :: Nil, _, _, _), _, _) =>
        RewriteResponse("help" :: "contact_us" :: Nil)
      case RewriteRequest(ParsePath("pay_info" :: Nil, _, _, _), _, _) =>
        RewriteResponse("help" :: "pay_info" :: Nil)
      case RewriteRequest(ParsePath("sitemap" :: Nil, _, _, _), _, _) =>
        RewriteResponse("help" :: "sitemap" :: Nil)

      case RewriteRequest(ParsePath("wenda" :: Nil, _, _, _), _, _) =>
        RewriteResponse("wenda" :: "index" :: Nil, Map("pageType" -> "0", "wendaTypeCode" -> "-1", "orderType" -> "0"))
      case RewriteRequest(ParsePath("wenda" :: AsInt(pageType) :: AsInt(wendaTypeCode) :: AsInt(orderType) :: Nil, _, _, _), _, _) =>
        RewriteResponse("wenda" :: "index" :: Nil, Map("pageType" -> pageType.toString, "wendaTypeCode" -> wendaTypeCode.toString, "orderType" -> orderType.toString))
      case RewriteRequest(ParsePath("wenda" :: AsInt(pageType) :: AsLong(id) :: Nil, _, _, _), _, _) =>
        RewriteResponse("wenda" :: "view" :: Nil, Map("pageType" -> pageType.toString, "id" -> id.toString))
      case RewriteRequest(ParsePath("wenda" :: AsInt(pageType) :: "res" :: AsLong(id) :: Nil, _, _, _), _, _) =>
        RewriteResponse("wenda" :: "resource" :: Nil, Map("pageType" -> pageType.toString, "id" -> id.toString))
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

    //User.autologinFunc = if (Props.devMode) Full(testUserLogin) else Empty

    WebCacheHelper.load()
  }
}
