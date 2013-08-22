package code.snippet.admin

import scala.util.Failure
import scala.util.Success
import scala.util.Try
import code.lib.BoxAlert
import code.model.ArticleType
import code.model.MessageType
import code.model.User
import code.snippet.SnippetHelper
import net.liftweb.common._
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.util.Helpers._
import code.model.Message
import code.model.UserMessage
import net.liftweb.mapper._

object Receivers {
  def unapply(receivers: String): Option[List[String]] = {
    if (!receivers.trim().isEmpty()) {
      Some(receivers.split(",").toList)
    } else {
      None
    }
  }
}

object MessageOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "create" => create
  }

  private def user = User.currentUser.openOrThrowException("not found user")

  def create = {
    var receivers = ""
    val message = Message.create
    def process(): JsCmd = {
      val ids = receivers match {
        case Receivers(receivers) => receivers.partition(id => Try { id.toLong }.isSuccess)
        case _ => (List[String](), List[String]())
      }

      if (!ids._2.isEmpty) {
        return BoxAlert("发送给数据中存在非法id，请确认！")
      }

      message.sender(0)
      message.save()

      ids._1.foreach(id => {
        UserMessage.find(By(UserMessage.user, id.toLong)) match {
          case Full(um) =>
            um.user(id.toLong)
            um.prependMessage(message.id.get)
            um.save()
          case _ =>
            val um = UserMessage.create
            um.user(id.toLong)
            um.prependMessage(message.id.get)
            um.save()
        }
      })

      BoxAlert("消息已经成功发送！", Reload)
    }

    val articleTypes = ArticleType.values.toList
    "@title" #> text(message.title.get, message.title(_)) &
      "@message_type" #> selectObj[MessageType.Value](MessageType.values.toList.map(v => (v, v.toString)), Full(message.messageType.is), message.messageType(_)) &
      "@receiver" #> textarea(receivers, receivers = _) &
      "@content" #> textarea(message.content.get, message.content(_)) &
      "type=submit" #> ajaxSubmit("发送", process)
  }

}