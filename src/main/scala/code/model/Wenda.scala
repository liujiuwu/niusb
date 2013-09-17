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
import scala.xml.NodeSeq
import code.lib.WebCacheHelper

object WendaType2 extends Enumeration {
  type WendaType2 = Value
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

  /*3=商标知识
5=商标注册
2=商标查询
4=商标分类
6=商标设计
7=商标取名
8=商标维权
9=驰名商标
10=商标转让
11=商标法*/
}

class Wenda extends LongKeyedMapper[Wenda] with CreatedUpdated with IdPK {
  def getSingleton = Wenda
  object title extends MappedString(this, 100) {
    def displayTitle = {
      <a href={ "/wenda/view/" + id.get } title={ this.get } target="_blank">{ this.get }</a>
    }
  }

  object wendaTypeCode extends MappedInt(this) {
    override def dbIndexed_? = true
    override def dbColumnName = "webda_type"
    override def displayName = "问答类型"

    def displayType: NodeSeq = {
      val wendType = WebCacheHelper.wendaTypes.get(this.is).get
      Text(wendType.name.is)
    }
  }

  object content extends MappedText(this)

  object asker extends MappedLong(this) {
    override def defaultValue = 0
    override def displayName = "提问人"
    override def dbColumnName = "ask_id"
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

  object replyCount extends MappedInt(this) {
    override def dbColumnName = "reply_count"
    def incr: Int = {
      this(this + 1)
      save
      this.is
    }
    def decr: Int = {
      val v = this.get - 1
      this(if (v < 0) 0 else v)
      save
      this.is
    }
  }

  object isRecommend extends MappedBoolean(this) { //是否推荐问题
    override def displayName = "推荐"
    override def defaultValue = false
    override def dbColumnName = "is_recommend"
    def displayRecommend = if (this.get) "是" else "否"
  }

  def replies = {
    val results = WendaReply.findAll(By(WendaReply.wenda, id.is));
    val (recommend, noRecommend) = results.partition(_.isRecommend.is)
    recommend :: noRecommend //将推荐答案放在最前面
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
  override def fieldOrder = List(id, title, wendaTypeCode, content, asker, readCount, createdAt, updatedAt)
}
