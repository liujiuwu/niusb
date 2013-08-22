package code.snippet.user

import scala.collection.mutable.ArrayBuffer
import scala.xml.NodeSeq
import code.lib.BoxConfirm
import code.model.Message
import code.model.User
import code.snippet.SnippetHelper
import net.liftweb.common._
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds.jsExpToJsCmd
import net.liftweb.mapper._
import net.liftweb.util.Helpers._
import net.liftweb.http.S
import net.liftweb.util.CssSel
import com.tristanhunt.knockoff.DefaultDiscounter._
import com.tristanhunt.knockoff._
import net.liftweb.http.MessageState
import code.model.UserMessage

class MessageOps extends DispatchSnippet with SnippetHelper with Loggable {
  val user = User.currentUser.get

  def dispatch = {
    case "list" => list
    case "view" => view
  }

  private def bies: List[QueryParam[Message]] = {
    val byBuffer = ArrayBuffer[QueryParam[Message]](OrderBy(Message.id, Descending))
    //byBuffer += By(Message.receiver, user.id.get)
    byBuffer.toList
  }

  def list = {
    def actions(message: Message): NodeSeq = {
      a(() => {
        BoxConfirm("确定删除【" + message.title.get + "】？此操作不可恢复，请谨慎！", {
          ajaxInvoke(() => { message.delete_!; JsCmds.Reload })._2
        })
      }, <i class="icon-trash"></i>, "class" -> "btn btn-danger")
    }

    var url = originalUri

    val messages = (UserMessage.find(By(UserMessage.user, user.id.get)) match {
      case Full(userMessage) =>
        userMessage.userMessages match {
          case Some(messageFlag) =>
            Some(for (mg <- messageFlag; msg <- Message.findByKey(mg.id)) yield {
              msg.isRead = mg.readFlag
              msg
            })
          case _ => None
        }
      case _ => None
    }) match {
      case Some(messages) => messages
      case _ => List[Message]()
    }

    val dataList = "#dataList tr" #> messages.map(message => {
      "#title" #> <a href={ "/user/sms/view?id=" + message.id.get }>{ message.title.get }</a> &
        "#messageType" #> message.messageType.asHtml &
        "#sender" #> message.sender.asHtml &
        "#createdAt" #> message.createdAt.asHtml &
        "#actions" #> actions(message)
    })
    dataList
  }

  def view = {
    tabMenuRV(Full("zoom-in" -> "查看信息"))

    (for {
      messageId <- S.param("id").flatMap(asLong) ?~ "消息ID不存在或无效"
      message <- Message.find(By(Message.id, messageId)) ?~ s"ID为${messageId}的消息不存在。"
    } yield {
      "#title *" #> message.title.get &
        "#content" #> toXHTML(knockoff(message.content.get))
    }): CssSel
  }
}