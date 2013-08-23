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
import code.model.ReceiverType
import scala.xml.Text
import code.model.UserData

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
    var receiverType = ReceiverType.All
    val message = Message.create
    message.sender(0)
    def process(): JsCmd = {
      message.receiverType(receiverType)
      message.receiver("")
      receiverType match {
        case ReceiverType.UserId =>
          val ids = receivers match {
            case Receivers(receivers) => receivers.partition(id => Try { id.toLong }.isSuccess)
            case _ => (List[String](), List[String]())
          }

          if (!ids._2.isEmpty) {
            return BoxAlert("发送给数据中存在非法Id，请确认！")
          }

          if (ids._1.isEmpty) {
            return BoxAlert("请输入接收用户Id，请确认！")
          }

          message.receiver(ids._1.mkString(","))
          if (message.save()) {
            ids._1.foreach(id => {
              UserData.find(By(UserData.user, id.toLong)) match {
                case Full(ud) =>
                  ud.user(id.toLong)
                  ud.prependMessage(message.id.get)
                  ud.save()
                case _ =>
                  val ud = UserData.create
                  ud.user(id.toLong)
                  ud.prependMessage(message.id.get)
                  ud.save()
              }
            })
          }
        case _ =>
          message.save()
      }

      BoxAlert("消息已经成功发送！", Reload)
    }

    val articleTypes = ArticleType.values.toList
    "@title" #> text(message.title.get, message.title(_)) &
      "@messageType" #> selectObj[MessageType.Value](MessageType.values.toList.map(v => (v, v.toString)), Full(message.messageType.is), message.messageType(_)) &
      "@receiverType" #> select(ReceiverType.values.toList.map(v => (v.id.toString, v.toString)), Some(receiverType.id.toString), v => receiverType = ReceiverType(v.toInt)) &
      "@receiver" #> textarea(receivers, receivers = _) &
      "@content" #> textarea(message.content.get, message.content(_)) &
      "type=submit" #> ajaxSubmit("发送", process)
  }

}