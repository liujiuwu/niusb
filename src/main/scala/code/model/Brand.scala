package code.model

import java.text.SimpleDateFormat
import code.lib.WebHelper
import net.liftweb.common.Box
import net.liftweb.common.Full
import net.liftweb.mapper.CRUDify
import net.liftweb.mapper.CreatedUpdated
import net.liftweb.mapper.IdPK
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.util.FieldError
import net.liftweb.mapper.MappedInt
import net.liftweb.mapper.MappedDate
import net.liftweb.mapper.MappedEnum
import net.liftweb.mapper.MappedString
import net.liftweb.mapper.MappedBoolean
import net.liftweb.mapper.MappedLong
import net.liftweb.http.S

object BrandStatus extends Enumeration {
  type BrandStatus = Value
  val ShenHeShiBai = Value(-1, "审核失败")
  val JiaoYiGuanBi = Value(-2, "交易关闭")
  val ShenHeZhong = Value(0, "审核中")
  val ChuShoZhong = Value(1, "出售中")
  val JiaoYiChengGong = Value(2, "交易成功")
}

class Brand extends LongKeyedMapper[Brand] with CreatedUpdated with IdPK {
  def getSingleton = Brand
  object userId extends MappedLong(this) {
    override def dbIndexed_? = true
    override def dbColumnName = "user_id"
  }

  object name extends MappedString(this, 20)

  object brandTypeId extends MappedInt(this) {
    override def dbIndexed_? = true
    override def dbColumnName = "brand_type_id"
  }

  object status extends MappedEnum(this, BrandStatus) {
    override def defaultValue = BrandStatus.ShenHeZhong
  }

  object regNo extends MappedString(this, 15) {
    override def dbIndexed_? = true
    override def dbColumnName = "reg_no"
    override def validations = valUnique("该商标注册号已经存在，请确认商标号正确！") _ :: super.validations
  }

  object regDate extends MappedDate(this) {
    override def dbColumnName = "reg_date"

    override def validations = {
      def isDate(txt: java.util.Date) = {
        if (txt == null)
          List(FieldError(this, "Please input a validate date."))
        else
          List[FieldError]()
      }

      isDate _ :: Nil
    }

    override def format(d: java.util.Date): String = WebHelper.fmtDateStr(d)

    override def parse(s: String): Box[java.util.Date] = {
      val df = new SimpleDateFormat("yyyy-MM-dd")
      try {
        val date = df.parse(s)
        Full(date)
      } catch {
        case _: Exception => Full(this.set(null))
      }
    }
  }

  object applicant extends MappedString(this, 15) {
    override def dbColumnName = "applicant"
  }

  object basePrice extends MappedInt(this) {
    override def dbColumnName = "base_price"
  }

  object sellPrice extends MappedInt(this) {
    override def dbColumnName = "sell_price"
  }

  object strikePrice extends MappedInt(this) {
    override def dbColumnName = "strike_price"
  }

  object soldDate extends MappedDate(this) {
    override def dbColumnName = "sold_date"
  }

  object useDescn extends MappedString(this, 200) {
    override def dbColumnName = "use_descn"
  }

  object descn extends MappedString(this, 300)

  object pic extends MappedString(this, 200)

  object adPic extends MappedString(this, 200) {
    override def dbColumnName = "ad_pic"
  }

  object concernCount extends MappedInt(this) {
    override def dbColumnName = "concern_count"
  }

  object recommend extends MappedBoolean(this) {
    override def defaultValue = false
  }

  object brandOrder extends MappedBoolean(this) {
    override def dbColumnName = "brand_order"
  }

}

object Brand extends Brand with CRUDify[Long, Brand] with LongKeyedMetaMapper[Brand] {
  override def dbTableName = "brands"

  override def fieldOrder = List(id, userId, name, brandTypeId, status, regNo, regDate, applicant, basePrice, sellPrice, strikePrice, soldDate, useDescn, descn, pic, adPic, concernCount, recommend, brandOrder, createdAt, updatedAt)

}
