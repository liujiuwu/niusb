package code.model

import net.liftweb.mapper._
import scala.collection.mutable.LinkedHashMap
import code.lib.WebCacheHelper
import scala.xml.NodeSeq
import scala.xml.Text

class WendaType extends LongKeyedMapper[WendaType] with IdPK {
  def getSingleton = WendaType

  object code extends MappedInt(this) {
    override def dbIndexed_? = true
  }

  object name extends MappedString(this, 30)

  object wendaCount extends MappedInt(this) {
    override def dbColumnName = "wenda_count"
    override def defaultValue = 0
    def incr(v: Int = 1) = {
      require(v > 0)
      this(this.is + v)
      save
    }

    def decr(v: Int = 1) = {
      require(v > 0)
      val nv = this.is - v
      this(if (nv < 0) 0 else nv)
      save
    }
  }

  object isRecommend extends MappedBoolean(this) { //是否推荐
    override def displayName = "推荐"
    override def defaultValue = false
    override def dbColumnName = "is_recommend"
    def displayRecommend = if (this.get) "是" else "否"
  }

  object descn extends MappedString(this, 600)
}

object WendaType extends WendaType with CRUDify[Long, WendaType] with LongKeyedMetaMapper[WendaType] {
  import com.niusb.util.WebHelpers._
  lazy val wendaTypes = WebCacheHelper.wendaTypes.values.toList
  override def dbTableName = "wenda_types"
  override def fieldOrder = List(id, code, name, wendaCount, isRecommend, descn)

  def wendaTypeOptions(selected: String): NodeSeq = {
    <option value="all">所有问答类型</option> :: (for (option <- wendaTypes; (value, label) = (option.code.get.toString, option.name.get)) yield {
      options(value, label, selected, true)
    })
  }
}