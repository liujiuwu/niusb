package code.model

import java.text.SimpleDateFormat
import net.liftweb.mapper._
import com.niusb.util.WebHelpers
import com.niusb.util.MemHelpers
import net.liftweb.util.Helpers._
import scala.xml.Unparsed
import scala.xml.Text
import scala.util.Try
import scala.util.Failure
import net.liftweb.common.Full
import scala.util.Success
import net.liftweb.common.Box
import java.util.Date

class WendaReply extends LongKeyedMapper[WendaReply] with CreatedUpdated with IdPK {
  def getSingleton = WendaReply

  object wenda extends MappedLongForeignKey(this, Wenda) {
    override def dbColumnName = "wenda_id"
  }

  object content extends MappedText(this)

  object reply extends MappedLong(this) {
    override def defaultValue = 0
    override def displayName = "回答人"
    override def dbColumnName = "reply_id"

    def replyer = User.find(By(User.id, this.is)) match {
      case Full(u) => Text(u.displayMaskName)
      case _ => <a href={ WebHelpers.WebSiteUrlAndName._1 } target="_blank">{ WebHelpers.WebSiteUrlAndName._2 }</a>
    }
  }

  object isRecommend extends MappedBoolean(this) { //是否推荐答案
    override def displayName = "推荐"
    override def defaultValue = false
    override def dbColumnName = "is_recommend"
    def displayRecommend = if (this.get) "是" else "否"
  }

  override lazy val createdAt = new MyCreatedAt(this) {
    override def dbColumnName = "created_at"
    override def format(date: java.util.Date): String = WebHelpers.fmtDateStr(date, WebHelpers.dfLongTime)
  }

  override lazy val updatedAt = new MyUpdatedAt(this) {
    override def dbColumnName = "updated_at"
    override def format(date: java.util.Date): String = WebHelpers.fmtDateStr(date, WebHelpers.dfLongTime)
  }
}

object WendaReply extends WendaReply with CRUDify[Long, WendaReply] with Paginator[WendaReply] {
  override def dbTableName = "wenda_replies"
  override def fieldOrder = List(id, wenda, content, reply, isRecommend, createdAt, updatedAt)
}
