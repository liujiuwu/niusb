package code.model

import java.text.SimpleDateFormat

import code.lib.WebHelper
import net.liftweb.common._
import net.liftweb.mapper._

object MessageType extends Enumeration {
  type MessageType = Value
  val System = Value(0, "系统消息")
}

object MessageStatus extends Enumeration {
  type MessageStatus = Value
  val Unread = Value(0, "未读")
  val Read = Value(1, "已读")
}

class Message extends LongKeyedMapper[Message] with CreatedUpdated with IdPK {
  def getSingleton = Message
  object title extends MappedString(this, 100)
  object messageType extends MappedEnum(this, MessageType) {
    override def defaultValue = MessageType.System
    override def dbColumnName = "message_type"
  }
  object receiver extends MappedLong(this) {
    override def dbColumnName = "receive_user_id"
  }
  object sender extends MappedLong(this) {
    override def dbColumnName = "send_user_id"
    def getSender = {
      User.find(By(User.id, sender.get)).openOrThrowException("not found user")
    }
  }
  object content extends MappedString(this, 600)

  object status extends MappedEnum(this, MessageStatus) {
    override def defaultValue = MessageStatus.Unread
  }

  override lazy val createdAt = new MyCreatedAt(this) {
    override def dbColumnName = "created_at"

    override def format(d: java.util.Date): String = WebHelper.fmtDateStr(d)

    override def parse(s: String): Box[java.util.Date] = {
      val df = new SimpleDateFormat("yyyy-MM-dd HH:mm")
      try {
        val date = df.parse(s)
        Full(date)
      } catch {
        case _: Exception => Full(this.set(null))
      }
    }
  }

  override lazy val updatedAt = new MyUpdatedAt(this) {
    override def dbColumnName = "updated_at"

    override def format(d: java.util.Date): String = WebHelper.fmtDateStr(d)

    override def parse(s: String): Box[java.util.Date] = {
      val df = new SimpleDateFormat("yyyy-MM-dd HH:mm")
      try {
        val date = df.parse(s)
        Full(date)
      } catch {
        case _: Exception => Full(this.set(null))
      }
    }
  }
}

object Message extends Message with CRUDify[Long, Message] with Paginator[Message] {
  override def dbTableName = "messages"
  override def fieldOrder = List(id, title, messageType, receiver, sender, status, content, createdAt, updatedAt)

}