package code.model

import net.liftweb.mapper._
import scala.collection.mutable.LinkedHashMap
import code.lib.WebCacheHelper

class BrandType extends LongKeyedMapper[BrandType] with IdPK {
  def getSingleton = BrandType

  object code extends MappedInt(this) {
    override def dbIndexed_? = true
  }

  object name extends MappedString(this, 30) {
    def displayTypeName() = { if (isRecommend.get) <span style="color:red;">{ code.get + "." + this.is }</span> else code.get + "." + this.is }
  }

  object brandCount extends MappedInt(this) {
    override def dbColumnName = "brand_count"
    override def defaultValue = 0
    def incr(v: Int = 1) = {
      require(v > 0)
      this(this.is + v)
    }

    def decr(v: Int = 1) = {
      require(v > 0)
      val nv = this.is - v
      this(if (nv < 0) 0 else nv)
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