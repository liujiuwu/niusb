package code.snippet.user

import scala.collection.mutable.ArrayBuffer
import scala.xml.NodeSeq
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
import net.liftweb.http.MessageState
import code.model.UserMessage
import code.model.PaginatorByMem
import code.model.UserData
import code.model.MessageFlagType
import code.model.UserType
import code.model.ReceiverType
import scala.xml.Unparsed
import com.niusb.util.WebHelpers._

object MessageOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "list" => list
    case "view" => view
  }

  private def bies: List[QueryParam[Message]] = {
    val byBuffer = ArrayBuffer[QueryParam[Message]](OrderBy(Message.id, Descending))
    //byBuffer += By(Message.receiver, user.id.get)
    byBuffer.toList
  }

  private def findVipUserMsgs() = {
    Message.findAll(By(Message.receiverType, ReceiverType.Vip), By_>=(Message.createdAt, loginUser.upgradedAt.get), OrderBy(Message.id, Descending))
  }

  private def findAgentUserMsgs() = {
    Message.findAll(By(Message.receiverType, ReceiverType.Agent), By_>=(Message.createdAt, loginUser.upgradedAt.get), OrderBy(Message.id, Descending))
  }
  private def findAllUserMsgs() = {
    Message.findAll(By(Message.receiverType, ReceiverType.All), By_>=(Message.createdAt, loginUser.createdAt.get), OrderBy(Message.id, Descending))
  }

  private def filterDelMsgs(userMsg: UserMessage, msg: Message): Boolean = {
    !(userMsg.flag == MessageFlagType.Del && msg.receiverType == ReceiverType.UserId)
  }

  def list = {
    val ud = UserData.getOrCreateUserData(loginUser.id.get)

    def actions(message: Message): NodeSeq = {
      a(() => {
        BoxConfirm("确定删除【" + message.title.get + "】？此操作不可恢复，请谨慎！", {
          ajaxInvoke(() => {
            ud.updateUserMessages(message.id.get, MessageFlagType.Del)
            JsCmds.Reload
          })._2
        })
      }, <i class="icon-trash"></i>, "class" -> "btn btn-danger")
    }

    var url = originalUri
    val messages = for {
      userMsg <- ud.userMessages
      if (userMsg.flag != MessageFlagType.Del)
      msg <- Message.findByKey(userMsg.messageId)
    } yield {
      msg.isRead = userMsg.flag == MessageFlagType.Readed
      msg
    }

    val sysMessages = (loginUser.userType.get match {
      case UserType.Vip => findVipUserMsgs()
      case UserType.Agent => findAgentUserMsgs()
      case _ => List[Message]()
    }) ::: findAllUserMsgs()

    val datas = sysMessages.filterNot(m => { ud.userMessages.exists(_.messageId == m.id.get) }) ::: messages

    val paginatorModel = PaginatorByMem.paginator(url, datas)()
    val dataList = "#dataList tr" #> paginatorModel.datas.map(message => {
      "#title" #> <a href={ "/user/sms/view?id=" + message.id.get }>{ message.title.get }</a> &
        "#messageType" #> message.messageType.asHtml &
        "#status" #> { if (message.isRead) "已读" else "未读" } &
        "#sender" #> message.sender.asHtml &
        "#createdAt" #> message.createdAt.asHtml &
        "#actions" #> actions(message)
    })
    dataList & "#pagination" #> paginatorModel.paginate _
  }

  def view = {
    tabMenuRV(Full("zoom-in" -> "查看信息"))
    (for {
      messageId <- S.param("id").flatMap(asLong) ?~ "消息ID不存在或无效"
      message <- Message.find(By(Message.id, messageId)) ?~ s"ID为${messageId}的消息不存在。"
      if (isView(messageId, message))
    } yield {
      "#title *" #> message.title.get &
        "#createdAt" #> message.createdAt.asHtml &
        "#messageType" #> message.messageType &
        "#sender" #> message.sender.get &
        "#msgContent *" #> Unparsed(message.content.get)
    }): CssSel
  }

  private def isView(messageId: Long, message: Message): Boolean = {
    val ud = UserData.getOrCreateUserData(loginUser.id.get)
    val enableView = message.receiverType.get match {
      case ReceiverType.All => loginUser.createdAt.get.before(message.createdAt.get)
      case ReceiverType.Vip => loginUser.userType.get == UserType.Vip && loginUser.upgradedAt.get.before(message.createdAt.get)
      case ReceiverType.Agent => loginUser.userType.get == UserType.Agent && loginUser.upgradedAt.get.before(message.createdAt.get)
      case ReceiverType.UserId => ud.userMessages.exists(_.messageId == messageId)
    }

    if (!enableView) {
      return enableView
    }
    ud.prependMessage(messageId, MessageFlagType.Readed)
  }

}