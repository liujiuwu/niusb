package code.snippet

import scala.xml.Text
import code.model.Wenda
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmd
import net.liftweb.util.Helpers._
import scala.xml.NodeSeq
import code.model.User
import code.model.WendaType
import code.lib.WebCacheHelper
import net.liftweb.common.Empty

object WendaOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "create" => create
    case "list" => list
    case "view" => view
  }

  def list = {
    "" #> Text("")
  }

  def view = {
    "" #> Text("")
  }

  def creteWendaBtn(wenda: Wenda): NodeSeq = {
    if (User.loggedIn_?) {
      <button class="btn btn-success" type="button" data-toggle="modal" data-target="#createWendaDialog">我要提问</button>
    } else {
      <span><a class="btn btn-small btn-success" data-toggle="modal" data-target="#loginDialog">注册登录</a> 后可以提问。</span>
    }
  }

  def create = {
    val wenda = Wenda.create
    def process(): JsCmd = {
      wenda.asker(loginUser.id.is)
      wenda.save()
      S.redirectTo("/wenda/index")
    }

    val wendaTypes = WebCacheHelper.wendaTypes.values.toList
    "@title" #> text(wenda.title.get, wenda.title(_)) &
      "@wendaType" #> select(wendaTypes.map(v => (v.code.is.toString, v.name.is)), Empty, v => wenda.wendaTypeCode(v.toInt)) &
      "@askContent" #> textarea(wenda.content.is, wenda.content(_)) &
      "type=submit" #> ajaxSubmit("发布", process)
  }

}