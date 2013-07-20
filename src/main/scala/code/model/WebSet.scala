package code.model

import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.CreatedUpdated
import net.liftweb.mapper.IdPK
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.mapper.CRUDify
import net.liftweb.mapper.MappedString
import net.liftweb.mapper.MappedInt

class WebSet extends LongKeyedMapper[WebSet] with IdPK {
  def getSingleton = WebSet

}

object WebSet extends WebSet with CRUDify[Long, WebSet] with LongKeyedMetaMapper[WebSet] {
  override def dbTableName = "webset"

  override def fieldOrder = List(id)
}