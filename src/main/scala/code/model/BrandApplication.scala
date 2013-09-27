package code.model

import java.util.Date

import com.niusb.util.WebHelpers

import net.liftweb.mapper._

object BrandApplicationStatus extends Enumeration {
  type BrandApplicationStatus = Value
  val ShenHeShiBai = Value(-1, "审核失败")
  val ShenHeZhong = Value(0, "审核中")
  val WaitPay = Value(1, "等待付款")
  val Paid = Value(2, "已付款")
  val SubmitBiaoJu = Value(3, "提交至商标局")
  val Accept = Value(4, "已受理")
  val Fail = Value(-2, "注册失败")
  val Success = Value(5, "注册成功")
}

class BrandApplication extends LongKeyedMapper[BrandApplication] with CreatedUpdated with IdPK {
  def getSingleton = BrandApplication

  object owner extends MappedLongForeignKey(this, User) {
    override def dbColumnName = "user_id"
    override def displayName = "申请用户"
  }

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

  object acceptInfo extends MappedString(this, 300) {
    override def displayName = "受理信息"
    override def dbColumnName = "accept_info"
  }

  object status extends MappedEnum(this, BrandApplicationStatus) {
    override def displayName = "商标注册状态"
    override def defaultValue = BrandApplicationStatus.ShenHeZhong
  }

  override lazy val createdAt = new MyCreatedAt(this) {
    override def displayName = "申请时间"
    override def dbColumnName = "created_at"
    override def format(date: java.util.Date): String = WebHelpers.fmtDateStr(date, WebHelpers.df)
  }

  override lazy val updatedAt = new MyUpdatedAt(this) {
    override def displayName = "最近更新时间"
    override def dbColumnName = "updated_at"
    override def format(date: java.util.Date): String = WebHelpers.fmtDateStr(date, WebHelpers.dfLongTime)
  }

}

object BrandApplication extends BrandApplication with CRUDify[Long, BrandApplication] with Paginator[BrandApplication] {
  override def dbTableName = "brand_applications"

  override def fieldOrder = List(id, brandName, brandTypeCode, name, contactInfo, additional, acceptInfo, remark, createdAt, updatedAt)
}