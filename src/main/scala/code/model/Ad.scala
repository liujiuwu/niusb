package code.model

import net.liftweb.mapper._

class Ad extends LongKeyedMapper[Ad] with IdPK {
  def getSingleton = Ad

  object adSpaceCode extends MappedInt(this) {
    override def dbColumnName = "ad_space_code"
  }

  object title extends MappedString(this, 30)
  object pic extends MappedString(this, 100)
  object link extends MappedString(this, 100)
  object descn extends MappedString(this, 600)
}

object Ad extends Ad with CRUDify[Long, Ad] with LongKeyedMetaMapper[Ad] {
  override def dbTableName = "ads"
  override def fieldOrder = List(id, adSpaceCode, title, pic, link, descn)

}