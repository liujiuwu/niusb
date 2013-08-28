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
case class UserFollow(brandId: Long) {
  override def toString = brandId.toString
}
object UserFollow {
  def unapply(follows: String): Option[List[UserFollow]] = {
    if (follows == null || follows.trim.isEmpty()) {
      return None
    }
    val fls = follows.split(",").toList
    Some(for (f <- fls) yield {
      UserFollow(f.toLong)
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

  def userMessages: List[UserMessage] = {
    messages.get match {
      case UserMessage(messages) => messages
      case _ => List[UserMessage]()
    }
  }

  def updateUserMessages(messageId: Long, flag: MessageFlagType.Value = MessageFlagType.Readed): Boolean = {
    val results = for (userMsg <- userMessages) yield {
      if (userMsg.messageId == messageId) UserMessage(messageId, flag) else userMsg
    }

    //删除不存在的消息及用户私人消息标记为删除的消息
    val msgs = for {
      userMsg <- results
      msg <- Message.findByKey(userMsg.messageId)
      if (!(userMsg.flag == MessageFlagType.Del && msg.receiverType.get == ReceiverType.UserId))
    } yield {
      userMsg
    }
    messages(msgs.sortWith((a, b) => a.messageId > b.messageId).mkString(","))
    save
  }

  def prependMessage(messageId: Long, flag: MessageFlagType.Value = MessageFlagType.UnRead): Boolean = {
    if (userMessages.exists(_.messageId == messageId)) { //消息已经存在，更新状态
      updateUserMessages(messageId, flag)
    } else {
      messages((UserMessage(messageId, flag) :: userMessages).mkString(","))
      save
    }
  }

  def userFollows: List[UserFollow] = {
    follows.get match {
      case UserFollow(follows) => follows
      case _ => List[UserFollow]()
    }
  }

  def isFollow(brandId: Long): Boolean = userFollows.exists(_.brandId == brandId)

  def cancelFollow(brandId: Long): Boolean = {
    if (!isFollow(brandId)) return false
    val userFlws = for {
      userFlw <- userFollows
      if (userFlw.brandId != brandId)
      brand <- Brand.findByKey(userFlw.brandId)
    } yield {
      userFlw
    }
    follows(userFlws.sortWith((a, b) => a.brandId > b.brandId).mkString(","))
    save
  }

  def prependFollow(brandId: Long): Boolean = {
    if (!isFollow(brandId)) {
      follows((UserFollow(brandId) :: userFollows).mkString(","))
      save
    } else {
      true
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
