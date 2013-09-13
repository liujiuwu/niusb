package code.snippet

import scala.xml._
import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http.DispatchSnippet

class TabMenuOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "tabMenu" => tabMenu
    case "helpNav" => helpNav
  }

  def tabMenu = {
    val menu: NodeSeq = tabMenuRV.get match {
      case Full((code, text)) if (!code.isEmpty() && !text.isEmpty()) => <li class="active"><a><i class={ "icon-" + code }></i>{ Text(" ") ++ text }</a></li>
      case _ => Text("")
    }
    "span" #> menu
  }

  def helpNav = {
    val url = originalUri

    "*" #> <ul class="nav nav-list">
             <li class={ if (url == "/about") "active" else null }><a href="/about">关于我们</a></li>
             <li class={ if (url == "/contact_us") "active" else null }><a href="/contact_us">联系我们</a></li>
             <li class={ if (url == "/pay_info") "active" else null }><a href="/pay_info">付款账户</a></li>
             <li class={ if (url.startsWith("/news")) "active" else null }><a href="/news">新闻公告</a></li>
             <li class={ if (url == "/sitemap") "active" else null }><a href="/sitemap">网站地图</a></li>
             <li class="divider"></li>
             <li class={ if (url == "/help/") "active" else null }><a href="/help/">帮助中心</a></li>
           </ul>
  }

}