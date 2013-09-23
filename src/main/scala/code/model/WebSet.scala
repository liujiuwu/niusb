package code.model

import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.CreatedUpdated
import net.liftweb.mapper.IdPK
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.mapper.CRUDify
import net.liftweb.mapper.MappedString
import net.liftweb.mapper.MappedInt

class Webset extends LongKeyedMapper[Webset] with IdPK {
  def getSingleton = Webset

  object basePriceFloat extends MappedInt(this) {
    override def dbColumnName = "base_price_float"
    override def displayName = "商标基价浮动百分比"
  }
}

object Webset extends Webset with CRUDify[Long, Webset] with LongKeyedMetaMapper[Webset] {
  override def dbTableName = "webset"

  override def fieldOrder = List(id)
}