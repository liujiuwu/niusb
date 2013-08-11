package code.model

import java.util.Date

import scala.collection.mutable.LinkedHashMap

import net.liftweb.mapper._

object AdvType extends Enumeration {
  type AdvType = Value
  val Normal = Value(0, "普通文字")
  val Pic = Value(1, "图片")
  val Slider = Value(2, "幻灯片")
}

class AdSpace extends LongKeyedMapper[AdSpace] with IdPK {
  def getSingleton = AdSpace

  object code extends MappedInt(this)
  object name extends MappedString(this, 30)
  object advType extends MappedEnum(this, AdvType) {
    override def defaultValue = AdvType.Normal
    override def dbColumnName = "adv_type"
  }
  object width extends MappedInt(this)
  object height extends MappedInt(this)
  object startTime extends MappedDateTime(this) {
    override def dbColumnName = "start_time"
  }
  object endTime extends MappedDateTime(this) {
    override def dbColumnName = "end_time"
  }

  object descn extends MappedString(this, 600)

  var ads = List[Ad]()
}

object AdSpace extends AdSpace with CRUDify[Long, AdSpace] with LongKeyedMetaMapper[AdSpace] {
  override def dbTableName = "ad_spaces"
  override def fieldOrder = List(id, code, name, advType, width, height, startTime, endTime, descn)

  private val adSpaces = LinkedHashMap[Int, AdSpace]()

  def loadAdSpace(force: Boolean = false) {
    if (adSpaces.isEmpty || force) {
      adSpaces.clear()
      val now = new Date
      findAll(By_<(AdSpace.startTime, now), By_>=(AdSpace.endTime, now)).foreach(adSpace => {
        adSpaces.put(adSpace.code.get, adSpace)
        adSpace.ads = Ad.findAll(By(Ad.adSpaceCode, adSpace.code.get))
      })
    }
  }

  def findByCode(code: Int) = {
    adSpaces.get(code)
  }
}