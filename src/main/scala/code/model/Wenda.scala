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

object WendaType extends Enumeration {
  type WendaType = Value
  val WendaType0 = Value(0, "商标知识")
  val WendaType1 = Value(1, "商标注册")
  val WendaType2 = Value(2, "商标查询")
  val WendaType3 = Value(3, "商标分类")
  val WendaType4 = Value(4, "商标设计")
  val WendaType5 = Value(5, "商标取名")
  val WendaType6 = Value(6, "商标维权")
  val WendaType7 = Value(7, "驰名商标")
  val WendaType8 = Value(8, "商标转让")
  val WendaType9 = Value(9, "商标法")
}

object WendaStatus extends Enumeration {
  type WendaStatus = Value
  val ToReplay = Value(0, "待回复")
  val Reply = Value(1, "已回复")
  val Close = Value(-1, "关闭问题")
}

class Wenda extends LongKeyedMapper[Wenda] with CreatedUpdated with IdPK {
  def getSingleton = Wenda
  object title extends MappedString(this, 100) {
    def displayTitle = {
      <a href={ "/wenda/view/" + id.get } title={ this.get } target="_blank">{ this.get }</a>
    }
  }

  object wendaType extends MappedEnum(this, WendaType) {
    override def defaultValue = WendaType.WendaType0
    override def dbColumnName = "webda_type"
  }

  object askContent extends MappedText(this)

  object asker extends MappedLong(this) {
    override def defaultValue = 0
    override def displayName = "提问人"
    override def dbColumnName = "ask_id"
  }

  object replyContent extends MappedText(this)

  object reply extends MappedLong(this) {
    override def defaultValue = 0
    override def displayName = "回答人"
    override def dbColumnName = "reply_id"
  }

  object replyDate extends MappedDate(this) {
    override def defaultValue = new Date
    override def displayName = "回答日期"
    override def dbColumnName = "reply_date"
    override def format(date: java.util.Date): String = WebHelpers.fmtDateStr(date, WebHelpers.dfLongTime)
  }

  object status extends MappedEnum(this, WendaStatus) {
    override def defaultValue = WendaStatus.ToReplay
  }

  object readCount extends MappedInt(this) {
    override def defaultValue = 0
    override def dbColumnName = "read_count"
    def incr(ip: String): Int = {
      val key = WebHelpers.memKey(ip, "wenda", id.get.toString())
      MemHelpers.get(key) match {
        case Some(time) =>
        case _ =>
          this(this + 1)
          save
          MemHelpers.set(key, 0, 10 minutes)
      }
      this.get
    }
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

object Wenda extends Wenda with CRUDify[Long, Wenda] with Paginator[Wenda] {
  override def dbTableName = "wendas"
  override def fieldOrder = List(id, title, wendaType, askContent, asker, readCount, replyContent, reply, createdAt, updatedAt)

  def validWendaTypeSelectValues = {
    val wendaTypes = WendaType.values.toList.map(v => (v.id.toString, v.toString))
    ("all", "所有类型") :: wendaTypes
  }
  def validStatusSelectValues = {
    val status = WendaStatus.values.toList.map(v => (v.id.toString, v.toString))
    ("all", "所有状态") :: status
  }
}
