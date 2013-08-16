package code.snippet.admin

import scala.xml._
import code.model.ArticleType
import code.model.Message
import code.model.MessageType
import code.model.User
import code.snippet.SnippetHelper
import net.liftweb.common._
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.SHtml
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import code.lib.BoxAlert
import net.liftweb.http.S
import net.liftweb.http.js.JsCmds

class MessageOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "create" => create
  }

  def create = {
    val message = Message.create
    def process(): JsCmd = {
      val user = User.currentUser.get
      message.sender(0)
      message.save()
      BoxAlert("消息发送成功！") & After(2 second, JsCmds.Reload)
    }

    val articleTypes = ArticleType.values.toList
    "@title" #> text(message.title.get, message.title(_)) &
      "@message_type" #> selectObj[MessageType.Value](MessageType.values.toList.map(v => (v, v.toString)), Full(message.messageType.is), message.messageType(_)) &
      "@content" #> textarea(message.content.get, message.content(_)) &
      "@sub" #> hidden(process)
  }

}