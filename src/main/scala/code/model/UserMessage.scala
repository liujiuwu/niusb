package code.model

import net.liftweb.mapper._

case class MessageFlag(id: Long, readFlag: Boolean = false) {
  override def toString = id + ":" + (if (readFlag) "1" else "0")
}

class UserMessage extends LongKeyedMapper[UserMessage] with CreatedUpdated with IdPK {
  def getSingleton = UserMessage

  object user extends MappedLong(this) {
    override def dbIndexed_? = true
    override def dbColumnName = "user_id"
  }

  object messages extends MappedText(this)

  def userMessages: Option[List[MessageFlag]] = {
    messages.get match {
      case UserMessage(messages) => Some(messages)
      case _ => None
    }
  }

  def prependMessage(id: Long) {
    messages(userMessages match {
      case Some(ms) =>
        (MessageFlag(id) :: ms).mkString(",")
      case _ => MessageFlag(id).toString()
    })
  }

}

object UserMessage extends UserMessage with CRUDify[Long, UserMessage] with Paginator[UserMessage] {
  override def dbTableName = "user_messages"

  def unapply(messages: String): Option[List[MessageFlag]] = {
    if (messages == null || messages.trim.isEmpty()) {
      return None
    }
    val msgs = messages.split(",").toList
    val rets = for (msg <- msgs) yield {
      val m = msg.split(":")
      MessageFlag(m(0).toLong, m(1) == "1")
    }
    Some(rets)
  }
}
