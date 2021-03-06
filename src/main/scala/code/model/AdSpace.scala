package code.model

import java.util.Date

import net.liftweb.mapper._

object AdSpaceType extends Enumeration {
  type AdSpaceType = Value
  val Normal = Value(0, "普通文字")
  val Pic = Value(1, "图片")
  val Slider = Value(2, "幻灯片")
  val friendlyLink = Value(3, "友情链接")
}

object AdSpaceStatus extends Enumeration {
  type AdSpaceStatus = Value
  val Normal = Value(0, "正常")
  val Close = Value(1, "关闭")
}

class AdSpace extends LongKeyedMapper[AdSpace] with IdPK {
  def getSingleton = AdSpace

  object code extends MappedInt(this)
  object name extends MappedString(this, 30)
  object adSpaceType extends MappedEnum(this, AdSpaceType) {
    override def defaultValue = AdSpaceType.Normal
    override def dbColumnName = "ad_space_type"
  }
  object width extends MappedInt(this)
  object height extends MappedInt(this)

  object status extends MappedEnum(this, AdSpaceStatus) {
    override def defaultValue = AdSpaceStatus.Normal
  }

  object descn extends MappedString(this, 600)

  var ads = List[Ad]()
}

object AdSpace extends AdSpace with CRUDify[Long, AdSpace] with LongKeyedMetaMapper[AdSpace] {
  override def dbTableName = "ad_spaces"
  override def fieldOrder = List(id, code, name, adSpaceType, width, height, status, descn)
}