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
import code.model.UserData
import code.model.MessageFlagType
import code.model.UserType
import code.model.ReceiverType

object MessageOps extends DispatchSnippet with SnippetHelper with Loggable {
  def user = User.currentUser.get

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
          ajaxInvoke(() => {
            for (ud <- UserData.find(By(UserData.user, user.id.get))) {
              ud.updateUserMessages(message.id.get, MessageFlagType.Del)
            }
            JsCmds.Reload
          })._2
        })
      }, <i class="icon-trash"></i>, "class" -> "btn btn-danger")
    }

    var url = originalUri
    val (messages, delMessages) = UserData.getOrCreateUserData(user.id.get).userMessages match {
      case Some(userMessages) =>
        val twoMsgs = userMessages.partition(_.flag != MessageFlagType.Del)
        val msg = for (userMsg <- twoMsgs._1; msg <- Message.findByKey(userMsg.messageId)) yield {
          msg.isRead = userMsg.flag == MessageFlagType.Readed
          msg
        }
        (msg, twoMsgs._2)
      case _ => (List[Message](), List[UserMessage]())
    }

    val allMsg = Message.findAll(By(Message.receiverType, ReceiverType.All), By_>=(Message.createdAt, user.createdAt), OrderBy(Message.id, Descending))
    val sysMessages = (user.userType.get match {
      case UserType.Vip =>
        Message.findAll(By(Message.receiverType, ReceiverType.Vip), By_>=(Message.createdAt, user.upgradedAt.get), OrderBy(Message.id, Descending))
      case UserType.Agent =>
        Message.findAll(By(Message.receiverType, ReceiverType.Agent), By_>=(Message.createdAt, user.upgradedAt.get), OrderBy(Message.id, Descending))
    }):::allMsg

    val datas = sysMessages.filterNot(m => (delMessages.exists(_.messageId == m.id.get) || messages.exists(_.id.get == m.id.get))) ::: messages
    val dataList = "#dataList tr" #> datas.map(message => {
      "#title" #> <a href={ "/user/sms/view?id=" + message.id.get }>{ message.title.get }</a> &
        "#messageType" #> message.messageType.asHtml &
        "#status" #> { if (message.isRead) "已读" else "未读" } &
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
      if (isView(messageId, message))
    } yield {
      "#title *" #> message.title.get &
        "#content" #> toXHTML(knockoff(message.content.get))
    }): CssSel
  }

  private def isView(messageId: Long, message: Message): Boolean = {
    val enableView = message.receiverType.get match {
      case ReceiverType.All =>
        user.createdAt.get.before(message.createdAt.get)
      case _ => user.userType.get match {
        case UserType.Normal => false
        case UserType.Vip => message.receiverType.get == ReceiverType.Vip
        case UserType.Agent => message.receiverType.get == ReceiverType.Agent
      }
    }

    if (!enableView) {
      return enableView
    }
    UserData.getOrCreateUserData(user.id.get).prependMessage(messageId, MessageFlagType.Readed)
  }

}