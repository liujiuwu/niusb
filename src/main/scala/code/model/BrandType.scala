package code.model

import net.liftweb.mapper._
import scala.collection.mutable.LinkedHashMap

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
}

object BrandType extends BrandType with CRUDify[Long, BrandType] with LongKeyedMetaMapper[BrandType] {
  override def dbTableName = "brand_types"
  override def fieldOrder = List(id, code, name, brandCount, recommend, descn)

  private val brandTypes = LinkedHashMap[Int, BrandType]()

  def getBrandTypes(force: Boolean = false): LinkedHashMap[Int, BrandType] = {
    if (brandTypes.isEmpty || force) {
      brandTypes.clear()
      findAll(OrderBy(BrandType.code, Ascending)).foreach(brandType => brandTypes.put(brandType.code.get, brandType))
    }
    brandTypes
  }
}