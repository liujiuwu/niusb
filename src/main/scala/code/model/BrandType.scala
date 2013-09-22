package code.model

import scala.collection.mutable.LinkedHashMap
import scala.xml.NodeSeq
import scala.xml.Text

import code.lib.WebCacheHelper
import net.liftweb.mapper._

class BrandType extends LongKeyedMapper[BrandType] with IdPK {
  def getSingleton = BrandType

  object code extends MappedInt(this) {
    override def dbIndexed_? = true
  }

  object name extends MappedString(this, 30) {
    def displayTypeName(): NodeSeq = {
      val oname = Text("第" + (if (code.is < 10) "0" + code.is else code.is) + "类-" + this.is + "(" + brandCount + ")")
      val dname = if (isRecommend.get) <span style="color:#E13335;">{ oname }</span> else oname
      <a href={ "/market/btc/" + code.is } title={ descn.is }>{ dname }</a>
    }
  }

  object brandCount extends MappedInt(this) {
    override def dbColumnName = "brand_count"
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

object BrandType extends BrandType with CRUDify[Long, BrandType] with LongKeyedMetaMapper[BrandType] {
  override def dbTableName = "brand_types"
  override def fieldOrder = List(id, code, name, brandCount, isRecommend, descn)
  def isBrandType(code: Int) = WebCacheHelper.brandTypes.contains(code)
}