package code.model

import java.text.SimpleDateFormat

import scala.xml._

import code.lib.UploadFileHelper
import code.lib.WebHelper
import net.liftweb.common._
import net.liftweb.mapper._
import net.liftweb.util._

object BrandStatus extends Enumeration {
  type BrandStatus = Value
  val ShenHeShiBai = Value(-1, "审核失败")
  val ShenHeZhong = Value(0, "审核中")
  val ChuShoZhong = Value(1, "出售中")
  val JiaoYiZhong = Value(2, "交易中")
  val ZantiJiaoYi = Value(3, "暂停交易")
  val JiaoYiChengGong = Value(4, "交易成功")
}

class Brand extends LongKeyedMapper[Brand] with CreatedUpdated with IdPK {
  def getSingleton = Brand
  object owner extends MappedLongForeignKey(this, User) {
    override def dbColumnName = "user_id"
    def getOwner = {
      User.find(By(User.id, owner.get)).openOrThrowException("not found user")
    }
  }

  object name extends MappedString(this, 50)

  object brandTypeCode extends MappedInt(this) {
    override def dbIndexed_? = true
    override def dbColumnName = "brand_type_code"

    def displayType: NodeSeq = {
      val brandType = BrandType.getBrandTypes().get(brandTypeCode.get).get
      <span>{ brandType.code + " -> " + brandType.name }</span>
    }
  }

  object status extends MappedEnum(this, BrandStatus) {
    override def defaultValue = BrandStatus.ShenHeZhong
    def displayStatus: NodeSeq = {
      status.get match {
        case BrandStatus.ShenHeShiBai => <span class="label label-important">{ BrandStatus.ShenHeShiBai }</span>
        case BrandStatus.ShenHeZhong => <span class="label">审核中</span>
        case BrandStatus.ChuShoZhong => <span class="label label-info">出售中</span>
        case BrandStatus.JiaoYiZhong => <span class="label label-warning">交易中</span>
        case BrandStatus.ZantiJiaoYi => <span class="label label-warning">{ BrandStatus.ZantiJiaoYi }</span>
        case BrandStatus.JiaoYiChengGong => <span class="label label-success">交易成功</span>
      }
    }
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
    def displayBasePrice: NodeSeq = WebHelper.badge("success", basePrice.get)
  }

  object sellPrice extends MappedInt(this) {
    override def dbColumnName = "sell_price"
    def displaySellPrice(forUser: Boolean = true, style: Boolean = false): NodeSeq = {
      val isFloatSellPrice = if (sellPrice.get >= basePrice.get) false else true
      val realSellPrice = if (sellPrice.get >= basePrice.get) sellPrice.get else basePrice.get + basePrice.get * 0.5
      val result = (realSellPrice / 10000) + "万"
      val displayLabel = if (forUser || !isFloatSellPrice) result else result + " - 浮"

      if (style) {
        WebHelper.badge("warning", displayLabel)
      } else {
        Text({ "￥" + displayLabel })
      }
    }

  }

  object strikePrice extends MappedInt(this) {
    override def dbColumnName = "strike_price"
    def displayStrikePrice: NodeSeq = WebHelper.badge("important", strikePrice.get)
  }

  object soldDate extends MappedDate(this) {
    override def dbColumnName = "sold_date"
  }

  object useDescn extends MappedString(this, 800) {
    override def dbColumnName = "use_descn"
  }

  object descn extends MappedString(this, 300)

  object pic extends MappedString(this, 100) {
    def displaySmallPic: NodeSeq = displayPic("brand-simg-box")

    def displayPic(css: String = "brand-bimg-box", alt: String = ""): NodeSeq = {
      <div class={ css }><img src={ src } alt={ alt }/></div>
    }

    def src = UploadFileHelper.srcPath(pic.get)
  }

  object adPic extends MappedString(this, 100) {
    override def dbColumnName = "ad_pic"
  }

  object lsqz extends MappedString(this, 300)

  object concernCount extends MappedInt(this) { //关注数
    override def dbColumnName = "concern_count"
  }

  object recommend extends MappedBoolean(this) { //是否推荐
    override def defaultValue = false
    def displayRecommend = if (recommend.get) "是" else "否"
  }

  object brandOrder extends MappedBoolean(this) {
    override def dbColumnName = "brand_order"
  }

  object isSelf extends MappedBoolean(this) {
    override def dbColumnName = "is_self"
    def displaySelf = if (isSelf.get) "是" else "否"
  }

  object remark extends MappedString(this, 300)

  object sellIndate extends MappedDate(this) {
    override def dbColumnName = "sell_indate"
  }

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

}

object Brand extends Brand with CRUDify[Long, Brand] with Paginator[Brand] {
  override def dbTableName = "brands"

  override def fieldOrder = List(id, owner, name, brandTypeCode, status, regNo, regDate, applicant, basePrice, sellPrice, strikePrice, soldDate, useDescn, descn, pic, adPic, concernCount, recommend, isSelf, remark, brandOrder, createdAt, updatedAt)

  def picName(pic: String, prefix: String = "s") = prefix + pic

  def validStatusSelectValues = {
    val status = BrandStatus.values.toList.map(v => (v.id.toString, v.toString))
    ("all", "所有状态") :: status
  }
}
