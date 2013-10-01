package code.snippet

import scala.xml._
import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http.DispatchSnippet
import scala.collection.mutable.LinkedHashMap

class TabMenuOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "topMainNav" => topMainNav
    case "tabMenu" => tabMenu
    case "helpNav" => helpNav
  }

  val navMenus = LinkedHashMap[String, NodeSeq](
    "/" -> <lift:children><i class="icon-home"></i> 首页</lift:children>,
    "/market" -> Text("商标集市"),
    "/brand" -> Text("商标注册"),
    "/wenda" -> Text("问答频道"))

  def topMainNav = {
    val url = originalUri
    "*" #> <ul class="nav navbar-nav">
             {
               for ((menu, idx) <- navMenus.zipWithIndex) yield {
                 val cls = if (idx == 0 && (url == "/" || url == "/index")) "active" else if (url.startsWith(menu._1) && idx > 0) "active" else null
                 <li class={ cls }><a href={ menu._1 }>{ menu._2 }</a></li>
               }
             }
           </ul>
  }

  def tabMenu = {
    val menu: NodeSeq = tabMenuRV.get match {
      case Full((code, text)) if (!code.isEmpty() && !text.isEmpty()) => <li class="active"><a><i class={ "icon-" + code }></i>{ Text(" ") ++ text }</a></li>
      case _ => Text("")
    }
    "span" #> menu
  }

  val helpNavMenus = LinkedHashMap[String, String](
    "/about" -> "关于我们",
    "/contact_us" -> "联系我们",
    "/pay_info" -> "付款账户",
    "/news" -> "新闻公告",
    "/sitemap" -> "网站地图",
    "/help" -> "帮助中心")

  def helpNav = {
    val url = originalUri

    "*" #> <div class="list-group">
             {
               for (menu <- helpNavMenus) yield {
                 val cls = if (url.startsWith(menu._1)) "active" else null
                 <a class={ "list-group-item " + cls } href={ menu._1 }>{ menu._2 }</a>
               }
             }
           </div>
  }

}