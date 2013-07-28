package code.model

import java.text.SimpleDateFormat

import scala.xml.NodeSeq

import code.lib.BrandTypeHelper
import code.lib.WebHelper
import code.rest.UploadManager
import net.liftweb.common._
import net.liftweb.mapper._
import net.liftweb.util._

object BrandStatus extends Enumeration {
  type BrandStatus = Value
  val ShenHeShiBai = Value(-1, "审核失败")
  val ShenHeZhong = Value(0, "审核中")
  val ChuShoZhong = Value(1, "出售中")
  val JiaoYiZhong = Value(2, "交易中")
  val JiaoYiChengGong = Value(3, "交易成功")
}

class Brand extends LongKeyedMapper[Brand] with CreatedUpdated with IdPK {
  def getSingleton = Brand
  object owner extends MappedLongForeignKey(this, User) {
    def getOwner = {
      User.find(By(User.id, owner.get)).openOrThrowException("not found user")
    }
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

  object isSelf extends MappedBoolean(this) {
    override def dbColumnName = "is_self"
  }

  object remark extends MappedString(this, 300)

  override lazy val createdAt = new MyCreatedAt(this) {
    override def dbColumnName = "created_at"

    override def format(d: java.util.Date): String = WebHelper.fmtDateStr(d)

    override def parse(s: String): Box[java.util.Date] = {
      val df = new SimpleDateFormat("yyyy-MM-dd HH:mm")
      try {
        val date = df.parse(s)
        Full(date)
      } catch {
        case _: Exception => Full(this.set(null))
      }
    }
  }

  override lazy val updatedAt = new MyUpdatedAt(this) {
    override def dbColumnName = "updated_at"

    override def format(d: java.util.Date): String = WebHelper.fmtDateStr(d)

    override def parse(s: String): Box[java.util.Date] = {
      val df = new SimpleDateFormat("yyyy-MM-dd HH:mm")
      try {
        val date = df.parse(s)
        Full(date)
      } catch {
        case _: Exception => Full(this.set(null))
      }
    }
  }

  def displayStatus: NodeSeq = {
    status.get match {
      case BrandStatus.ShenHeShiBai => <span class="label label-important">审核失败</span>
      case BrandStatus.ShenHeZhong => <span class="label">审核中</span>
      case BrandStatus.ChuShoZhong => <span class="label label-info">出售中</span>
      case BrandStatus.JiaoYiZhong => <span class="label label-warning">交易中</span>
      case BrandStatus.JiaoYiChengGong => <span class="label label-success">交易成功</span>
    }
  }

  def displaySpic: NodeSeq = displayPic("brand-simg-box", "128")

  def displayPic(css: String = "brand-bimg-box", size: String = "320"): NodeSeq = {
    <div class={ css }><img src={ displayPicSrc(size) } alt={ name.get }/></div>
  }

  def displayPicSrc(size: String = "320") = UploadManager.srcPath(UploadManager.sizePicName(pic.get, size))

  def displayType: NodeSeq = {
    val brandType = BrandTypeHelper.brandTypes.get(brandTypeId.get).get
    <span>{ brandType.id + " -> " + brandType.name }</span>
  }

  def displayBasePrice: NodeSeq = badge("success", basePrice.get)
  def displaySellPrice: NodeSeq = badge("warning", sellPrice.get)
  def displayStrikePrice: NodeSeq = badge("important", strikePrice.get)
  private def badge(state: String, data: AnyVal) = <span class={ "badge badge-" + state }>￥{ data }</span>

}

object Brand extends Brand with CRUDify[Long, Brand] with Paginator[Brand] {
  override def dbTableName = "brands"

  override def fieldOrder = List(id, owner, name, brandTypeId, status, regNo, regDate, applicant, basePrice, sellPrice, strikePrice, soldDate, useDescn, descn, pic, adPic, concernCount, recommend, isSelf, remark, brandOrder, createdAt, updatedAt)

  def picName(pic: String, prefix: String = "s") = prefix + pic

  def validStatusSelectValues = {
    val status = BrandStatus.values.toList.map(v => (v.id.toString, v.toString))
    val list = ("all", "所有状态") :: status
    Full(list)
  }
}
