package code.snippet.admin

import scala.util._
import code.lib.BoxAlert
import code.model.ArticleType
import code.model.MessageType
import code.model.User
import code.snippet.SnippetHelper
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.util.Helpers._
import code.model.Message
import code.model.UserMessage
import net.liftweb.mapper._
import code.model.ReceiverType
import scala.xml.Text
import code.model.UserData
import net.liftweb.http.SHtml._
import net.liftweb.http.S._
import code.lib.BoxConfirm
import scala.xml.NodeSeq
import net.liftweb.http.js.JE.JsRaw

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
    case "list" => list
  }

  private def user = User.currentUser.openOrThrowException("not found user")

  def create = {
    tabMenuRV(Full("plus" -> "发送消息"))

    var receivers = ""
    var receiverType = ReceiverType.All
    val message = Message.create
    message.sender(user.id.get)
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

    "@title" #> text(message.title.get, message.title(_)) &
      "@messageType" #> selectObj[MessageType.Value](MessageType.values.toList.map(v => (v, v.toString)), Full(message.messageType.is), message.messageType(_)) &
      "@receiverType" #> select(ReceiverType.values.toList.map(v => (v.id.toString, v.toString)), Some(receiverType.id.toString), v => receiverType = ReceiverType(v.toInt)) &
      "@receiver" #> textarea(receivers, receivers = _) &
      "@msgContent" #> textarea(message.content.get, message.content(_)) &
      "type=submit" #> ajaxSubmit("发送", process)
  }

  def list = {
    def actions(message: Message): NodeSeq = {
      a(() => {
        BoxConfirm("确定删除【" + message.title.get + "】？此操作不可恢复，请谨慎！", {
          ajaxInvoke(() => { message.delete_!; Reload })._2
        })
      }, <i class="icon-trash"></i>, "class" -> "btn btn-danger")
    }

    def viewMsg(message: Message): JsCmd = {
      JsRaw("$('#msgTitle').text('" + message.title.get + "')") &
        JsRaw("$('#msgInfo').text('" + message.receiverType.get + "')") &
        JsRaw("$('#msgContent').text('" + message.content.get + "')") &
        {
          if (message.receiverType.get == ReceiverType.UserId) {
            JsRaw("$('#msgInfo').text('接收用户Id:" + message.receiver.get + "')")
          }else{
            JsRaw("$('#msgInfo').text('')")
          }
        } &
        JsRaw("""$("#viewMsg").modal('show')""")
    }

    var url = originalUri
    val paginatorModel = Message.paginator(url)()

    val searchForm = "#searchForm" #>
      <form class="form-inline" action={ url } method="get">
        <a href="/admin/sms/create" class="btn btn-primary"><i class="icon-plus"></i> 发送消息</a>
      </form>

    val dataList = "#dataList tr" #> paginatorModel.datas.map(message => {
      val title = message.title.get
      //"#title" #> <a href="#">{ message.title.get }</a> andThen " [onclick]" #> Text("") &
      "#title" #> SHtml.a(() => viewMsg(message), Text(message.title.get)) &
        "#messageType" #> message.messageType &
        "#sender" #> message.sender.get &
        "#receiverType" #> message.receiverType &
        "#createdAt" #> message.createdAt.asHtml &
        "#actions" #> actions(message)
    })
    searchForm & dataList & "#pagination" #> paginatorModel.paginate _
  }

}