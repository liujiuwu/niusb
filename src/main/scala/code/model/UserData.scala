package code.model

import net.liftweb.mapper._
import net.liftweb.common.Full

object MessageFlagType extends Enumeration {
  type MessageFlagType = Value
  val Del = Value(-1, "删除")
  val UnRead = Value(0, "未读")
  val Readed = Value(1, "已读")
}

case class UserMessage(messageId: Long, flag: MessageFlagType.Value = MessageFlagType.UnRead) {
  override def toString = messageId + ":" + flag.id
}
object UserMessage {
  def unapply(messages: String): Option[List[UserMessage]] = {
    if (messages == null || messages.trim.isEmpty()) {
      return None
    }
    val msgs = messages.split(",").toList
    Some(for (msg <- msgs; items = msg.split(":")) yield {
      UserMessage(items(0).toLong, MessageFlagType(items(1).toInt))
    })
  }
}
case class UserFollow(brandId: Long, flag: Int = 0) {
  override def toString = brandId + ":" + flag
}
object UserFollow {
  def unapply(follows: String): Option[List[UserFollow]] = {
    if (follows == null || follows.trim.isEmpty()) {
      return None
    }
    val fls = follows.split(",").toList
    Some(for (f <- fls; items = f.split(":")) yield {
      UserFollow(items(0).toLong, items(1).toInt)
    })
  }
}

class UserData extends LongKeyedMapper[UserData] with CreatedUpdated with IdPK {
  def getSingleton = UserData

  object user extends MappedLong(this) {
    override def dbIndexed_? = true
    override def dbColumnName = "user_id"
  }

  object messages extends MappedText(this)
  object follows extends MappedText(this)

  def userMessages: Option[List[UserMessage]] = {
    messages.get match {
      case UserMessage(messages) => Some(messages)
      case _ => None
    }
  }

  def updateUserMessages(messageId: Long, flag: MessageFlagType.Value = MessageFlagType.Readed): Boolean = {
    userMessages match {
      case Some(userMsgs) =>
        val results = (for (userMsg <- userMsgs) yield {
          if (userMsg.flag != MessageFlagType.Del && userMsg.messageId == messageId) {
            UserMessage(messageId, flag)
          } else {
            userMsg
          }
        })
        messages(results.mkString(","))
        save
      case _ => false
    }
  }

  def userFollows: Option[List[UserFollow]] = {
    messages.get match {
      case UserFollow(follows) => Some(follows)
      case _ => None
    }
  }

  def updateUserFollows(brandId: Long, flag: Int = 1): Boolean = {
    userFollows match {
      case Some(userFlws) =>
        val results = (for (userFlw <- userFlws) yield {
          if (userFlw.brandId == brandId) {
            UserFollow(brandId, flag)
          } else {
            userFlw
          }
        })
        follows(results.mkString(","))
        save
      case _ => false
    }
  }

  def prependMessage(messageId: Long, flag: MessageFlagType.Value = MessageFlagType.UnRead): Boolean = {
    val messagesStr = userMessages match {
      case Some(ms) =>
        if (ms.exists(_.messageId == messageId)) None else Some((UserMessage(messageId, flag) :: ms).mkString(","))
      case _ => Some(UserMessage(messageId, flag).toString())
    }

    messagesStr match {
      case Some(msgstr) =>
        messages(msgstr)
        save
      case None => updateUserMessages(messageId) //消息已经存在，更新状态
    }
  }
}

object UserData extends UserData with CRUDify[Long, UserData] with Paginator[UserData] {
  override def dbTableName = "user_datas"

  def getOrCreateUserData(userId: Long) = {
    UserData.find(By(UserData.user, userId)) match {
      case Full(ud) => ud
      case _ =>
        val ud = UserData.create.user(userId)
        ud.save
        ud
    }
  }
}
