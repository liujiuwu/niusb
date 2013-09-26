package code.model

import net.liftweb.mapper._
import com.niusb.util.WebHelpers
import java.util.Date

class BrandApplication extends LongKeyedMapper[BrandApplication] with CreatedUpdated with IdPK {
  def getSingleton = BrandApplication

  object brandName extends MappedString(this, 40) {
    override def dbColumnName = "brand_name"
    override def displayName = "商标名称"
  }

  object brandTypeCode extends MappedInt(this) {
    override def dbIndexed_? = true
    override def dbColumnName = "brand_type_code"
    override def displayName = "商标类型"
  }

  object name extends MappedString(this, 50) {
    override def displayName = "联系人"
  }

  object contactInfo extends MappedString(this, 100) {
    override def dbColumnName = "contact_info"
    override def displayName = "联系方式"
  }

  object additional extends MappedString(this, 500) {
    override def displayName = "附加说明"
  }

  object remark extends MappedString(this, 500) {
    override def displayName = "客服备注"
  }

  object isAccept extends MappedBoolean(this) {
    override def displayName = "是否受理"
    override def defaultValue = false
    override def dbColumnName = "is_accept"
    def displayAccept = if (this.is) "是" else "否"
  }

  object acceptInfo extends MappedString(this, 300) {
    override def displayName = "受理信息"
    override def dbColumnName = "accept_info"
  }

  override lazy val createdAt = new MyCreatedAt(this) {
    override def displayName = "申请时间"
    override def dbColumnName = "created_at"
    override def format(date: java.util.Date): String = WebHelpers.fmtDateStr(date, WebHelpers.dfLongTime)
  }

  override lazy val updatedAt = new MyUpdatedAt(this) {
    override def displayName = "最近更新时间"
    override def dbColumnName = "updated_at"
    override def format(date: java.util.Date): String = WebHelpers.fmtDateStr(date, WebHelpers.dfLongTime)
  }

}

object BrandApplication extends BrandApplication with CRUDify[Long, BrandApplication] with LongKeyedMetaMapper[BrandApplication] {
  override def dbTableName = "brand_applications"

  override def fieldOrder = List(id, brandName, brandTypeCode, name, contactInfo, additional, isAccept, acceptInfo, remark, createdAt, updatedAt)
}