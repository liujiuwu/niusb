package code.model

import net.liftweb.mapper._

case class UserMessage(messageId: Long, flag: Int = 0) { //0=未读 ,1=已读 ,2=删除 
  override def toString = messageId + ":" + flag
}
object UserMessage {
  def unapply(messages: String): Option[List[UserMessage]] = {
    if (messages == null || messages.trim.isEmpty()) {
      return None
    }
    val msgs = messages.split(",").toList
    Some(for (msg <- msgs; items = msg.split(":")) yield {
      UserMessage(items(0).toLong, items(1).toInt)
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

  def updateUserMessages(messageId: Long, flag: Int = 1) {
    userMessages match {
      case Some(userMsgs) =>
        val results = (for (userMsg <- userMsgs) yield {
          if (userMsg.messageId == messageId) {
            UserMessage(messageId, flag)
          } else {
            userMsg
          }
        })
        messages(results.mkString(","))
        save
      case _ =>
    }
  }

  def userFollows: Option[List[UserFollow]] = {
    messages.get match {
      case UserFollow(follows) => Some(follows)
      case _ => None
    }
  }

  def updateUserFollows(brandId: Long, flag: Int = 1) {
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
      case _ =>
    }
  }

  def prependMessage(id: Long) {
    messages(userMessages match {
      case Some(ms) =>
        (UserMessage(id) :: ms).mkString(",")
      case _ => UserMessage(id).toString()
    })
  }

}

object UserData extends UserData with CRUDify[Long, UserData] with Paginator[UserData] {
  override def dbTableName = "user_datas"
}
