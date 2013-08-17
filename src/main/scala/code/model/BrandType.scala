package code.model

import net.liftweb.mapper._
import scala.collection.mutable.LinkedHashMap
import code.lib.WebCacheHelper

class BrandType extends LongKeyedMapper[BrandType] with IdPK {
  def getSingleton = BrandType

  object code extends MappedInt(this) {
    override def dbIndexed_? = true
  }

  object name extends MappedString(this, 30)

  object brandCount extends MappedInt(this) {
    override def dbColumnName = "brand_count"
    override def defaultValue = 0
  }

  object recommend extends MappedBoolean(this) { //是否推荐
    override def defaultValue = false
  }

  object descn extends MappedString(this, 600)

  def incrBrandCount(v: Int = 1) = {
    require(v > 0)
    brandCount(brandCount.get + v)
  }

  def decrBrandCount(v: Int = 1) = {
    require(v > 0)
    val nv = brandCount.get - v
    brandCount(if (nv < 0) 0 else nv)
  }

  def displayTypeName() = { if (recommend.get) <span style="color:red;">{ code.get + "." + name.get }</span> else code.get + "." + name.get }

}

object BrandType extends BrandType with CRUDify[Long, BrandType] with LongKeyedMetaMapper[BrandType] {
  override def dbTableName = "brand_types"
  override def fieldOrder = List(id, code, name, brandCount, recommend, descn)
  def isBrandType(code: Int) = WebCacheHelper.brandTypes.contains(code)
}