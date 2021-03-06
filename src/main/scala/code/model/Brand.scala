package code.model

import java.text.SimpleDateFormat
import java.util.Date
import scala.language.postfixOps
import scala.util.Success
import scala.util.Try
import scala.xml._
import com.niusb.util.MemHelpers
import com.niusb.util.UploadHelpers
import com.niusb.util.WebHelpers
import com.niusb.util.WebHelpers._
import code.lib.WebCacheHelper
import net.liftweb.common._
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import com.niusb.util.SearchBrandFormHelpers
import scala.util.Random

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
    override def displayName = "商标所属人"
    def getOwner = {
      if (owner.is > 0) {
        User.find(By(User.id, owner.is))
      } else {
        Empty
      }
    }

    def display = {
      getOwner match {
        case Full(user) => user.displayInfo
        case _ => Text("")
      }
    }
  }

  object name extends MappedString(this, 50) {
    override def displayName = "商标名称"
  }

  object brandTypeCode extends MappedInt(this) {
    override def dbIndexed_? = true
    override def dbColumnName = "brand_type_code"
    override def displayName = "商标类型"

    def displayType: NodeSeq = {
      val brandType = WebCacheHelper.brandTypes.get(brandTypeCode.get).get
      Text(brandType.code + " -> " + brandType.name)
    }

    def displayTypeLabel: NodeSeq = {
      Text(brandTypeCode + "类")
    }
  }

  object status extends MappedEnum(this, BrandStatus) {
    override def displayName = "商标状态"
    override def defaultValue = BrandStatus.ShenHeZhong
    def displayStatus: NodeSeq = {
      status.get match {
        case BrandStatus.ShenHeShiBai => <span class="label label-danger">{ BrandStatus.ShenHeShiBai }</span>
        case BrandStatus.ShenHeZhong => <span class="label label-default">审核中</span>
        case BrandStatus.ChuShoZhong => <span class="label label-primary">出售中</span>
        case BrandStatus.JiaoYiZhong => <span class="label label-warning">交易中</span>
        case BrandStatus.ZantiJiaoYi => <span class="label label-warning">{ BrandStatus.ZantiJiaoYi }</span>
        case BrandStatus.JiaoYiChengGong => <span class="label label-success">交易成功</span>
      }
    }
  }

  object regNo extends MappedString(this, 15) {
    override def displayName = "商标注册号"
    override def dbIndexed_? = true
    override def dbColumnName = "reg_no"
    override def validations = valUnique("该商标注册号已经存在，请确认商标号正确！") _ :: super.validations
  }

  object regDate extends MappedDate(this) {
    override def displayName = "商标注册日期"
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

    override def format(date: java.util.Date): String = WebHelpers.fmtDateStr(date, WebHelpers.df)

    override def parse(s: String): Box[java.util.Date] = {
      Try { WebHelpers.df.parse(s) } match {
        case Success(date) => Full(date)
        case _ => Full(new Date)
      }
    }
  }

  object applicant extends MappedString(this, 15) {
    override def dbColumnName = "applicant"
    override def displayName = "申请人"
  }

  object basePrice extends MappedInt(this) {
    override def displayName = "商标基价"
    override def dbIndexed_? = true
    override def dbColumnName = "base_price"
    def displayBasePrice: NodeSeq = WebHelpers.badge("success", basePrice.get)
  }

  object sellPrice extends MappedInt(this) {
    override def displayName = "商标出售价"
    override def dbColumnName = "sell_price"
    def displaySellPrice(forUser: Boolean = true, style: Boolean = false): NodeSeq = {
      val isQuote = basePrice.get > 0
      val isFloatSellPrice = if (sellPrice.get >= basePrice.get || !isQuote) false else true
      val basePriceFloat = WebCacheHelper.websets.values.headOption match {
        case Some(webset) => (webset.basePriceFloat.is / 100.0f)
        case _ => 1f
      }
      val realSellPrice = if (sellPrice.get >= basePrice.get) sellPrice.get else basePrice.get + basePrice.get * basePriceFloat
      val result = if (realSellPrice <= 0) "面议" else (realSellPrice / 10000) + "万"
      val displayLabel = if (forUser || !isFloatSellPrice) result else result + " - 浮"

      if (style) {
        WebHelpers.badge("warning", displayLabel)
      } else {
        Text({ (if (isQuote) "￥" else "") + displayLabel })
      }
    }

  }

  object strikePrice extends MappedInt(this) {
    override def displayName = "商标成交价"
    override def dbColumnName = "strike_price"
    def displayStrikePrice: NodeSeq = WebHelpers.badge("important", strikePrice.get)
  }

  object soldDate extends MappedDate(this) {
    override def displayName = "成交日期"
    override def dbColumnName = "sold_date"
  }

  object useDescn extends MappedString(this, 800) {
    override def displayName = "商标使用描述"
    override def dbColumnName = "use_descn"
  }

  object descn extends MappedString(this, 300) {
    override def displayName = "商标创意说明"

    def display = {
      if (this.is != null && !this.is.isEmpty()) {
        this.is
      } else {
        val idx = (id.is % Brand.descns.length.toLong).toInt
        Brand.descns(idx).replaceAll("name", brandName).replaceAll("useDescn", useDescn.is).replaceAll("brandTypeCode", brandTypeCode.is.toString)
      }
    }
  }

  object pic extends MappedString(this, 100) {
    override def displayName = "商标图"
    def displaySmallPic: NodeSeq = displayPic("brand-simg-box")

    def displayPic(css: String = "brand-bimg-box", alt: String = ""): NodeSeq = {
      <img src={ src } alt={ alt } height="200" width="320" class={ css }/>
    }

    def src = UploadHelpers.srcPath(pic.get)
  }

  object adPic extends MappedString(this, 100) {
    override def displayName = "商标广告图"
    override def dbColumnName = "ad_pic"
    def adPics: Box[List[String]] = {
      if (!this.is.isEmpty()) {
        val tempAds = this.is.split(";").toList
        Full(tempAds.filter(_.endsWith(".jpg")))
      } else {
        Empty
      }
    }
  }

  object lsqz extends MappedString(this, 300) {
    override def displayName = "商标类似群组"
  }

  object followCount extends MappedInt(this) { //关注数
    override def defaultValue = 0
    override def displayName = "关注数"
    override def dbColumnName = "follow_count"
    def incr: Int = {
      this(this + 1)
      save
      this.is
    }
    def decr: Int = {
      val v = this.get - 1
      this(if (v < 0) 0 else v)
      save
      this.is
    }
  }

  object viewCount extends MappedInt(this) { //查看数
    override def defaultValue = 0
    override def displayName = "查看数"
    override def dbColumnName = "view_count"
    def incr(ip: String): Int = {
      val key = WebHelpers.memKey(ip, "brand", id.get.toString())
      MemHelpers.get(key) match {
        case Some(time) =>
        case _ =>
          this(this + 1)
          save
          MemHelpers.set(key, 0, 10 minutes)
      }
      this.get
    }
  }

  object isRecommend extends MappedBoolean(this) { //是否推荐
    override def displayName = "推荐"
    override def defaultValue = false
    override def dbColumnName = "is_recommend"
    def displayRecommend = if (this.get) "是" else "否"
  }

  object isOffer extends MappedBoolean(this) { //是否特价
    override def displayName = "是否特价"
    override def dbColumnName = "is_offer"
    override def defaultValue = false
    def displayOffer = if (this.get) "是" else "否"
  }

  object brandOrder extends MappedBoolean(this) {
    override def displayName = "排序"
    override def dbColumnName = "brand_order"
  }

  object isOwn extends MappedBoolean(this) {
    override def displayName = "自有商标"
    override def dbColumnName = "is_own"
    def displayOwn = if (this.get) "是" else "否"
  }

  object remark extends MappedString(this, 500) {
    override def displayName = "备注"
  }

  override lazy val createdAt = new MyCreatedAt(this) {
    override def displayName = "发布时间"
    override def dbColumnName = "created_at"
    override def format(date: java.util.Date): String = WebHelpers.fmtDateStr(date, WebHelpers.dfLongTime)
  }

  override lazy val updatedAt = new MyUpdatedAt(this) {
    override def displayName = "最近更新时间"
    override def dbColumnName = "updated_at"
    override def format(date: java.util.Date): String = WebHelpers.fmtDateStr(date, WebHelpers.dfLongTime)
  }

  def displayBrand = {
    def viewLink(c: NodeSeq) = <a href={ "/market/" + id.get } target="_blank">{ c }</a>
    "img" #> viewLink(<img class="brand-img lazy" src="/img/transparent.gif" data-original={ pic.src } alt={ name.is.trim }/>) &
      ".brandTypeCode *" #> brandTypeCode.displayTypeLabel &
      ".price *" #> sellPrice.displaySellPrice() &
      ".brand-name *" #> viewLink(Text(name.get.trim)) &
      ".viewCount *+" #> <em>{ viewCount.is }</em> &
      ".followCount *+" #> <em>{ followCount.is }</em>
  }

  def delBrandAndUpdateBrandType = {
    val brandTypeCode = this.brandTypeCode.is
    WebCacheHelper.brandTypes.get(brandTypeCode) match {
      case Some(brandType) => brandType.brandCount.decr()
      case _ =>
    }
    delete_!
  }

  def brandName = name.is

  def saveAndUpdateBrandType(newStatus: BrandStatus.Value) = {
    if (status == BrandStatus.ShenHeZhong && newStatus == BrandStatus.ChuShoZhong) {
      val brandTypeCode = this.brandTypeCode.is
      WebCacheHelper.brandTypes.get(brandTypeCode) match {
        case Some(brandType) => brandType.brandCount.incr()
        case _ =>
      }
    }
    status(newStatus)
    save
  }
}

object Brand extends Brand with CRUDify[Long, Brand] with Paginator[Brand] {
  override def dbTableName = "brands"

  override def fieldOrder = List(id, owner, name, brandTypeCode, status, regNo, regDate, applicant, basePrice, sellPrice, strikePrice, soldDate, useDescn, descn, pic, adPic, followCount, isRecommend, isOwn, isOffer, remark, brandOrder, createdAt, updatedAt)

  def picName(pic: String, prefix: String = "s") = prefix + pic

  val descns = List[String](
    "name的创意根据中国改革开放以来,对世界近万类产品标识进行分析总结,多位资深品牌包装人士认定,设计创意完全符合第1类商标，useDescn的特征。一个好的商标是企业成功的基石，是产品快速进入市场、抢占市场的利器。name商标有助于您快速产品上线、打开市场，从而广受到消费者欢迎。好商标——成就一个企业，推动一个行业。",
    "name的创意符合第brandTypeCode类商标，useDescn的特征。一个牛的商标是企业成功的基石，name商标有助于您打开市场，受到消费者欢迎。牛商标——成就一个企业，推动一个行业。",
    "朗朗上口的name商标是第brandTypeCode类商标，设计风格独特，构思大胆严谨，name易于广大消费者快速识别，name的设计风格，同时符合使用在useDescn的相关特征。商标名是走向市场的第一步，是产品形象品质的象征。name商标有助于您快速产 品上线、打开市场，从而广受到消费者欢迎。牛商标——成就一个企业，推动一个行业。",
    "好听易记的name商标设计风格独特，构思大胆严谨，永驰的设计风格易于广大消费者快速识别，同时符合使用在useDescn的相关特征。商标名是走向市场的第一步，是产品形象品质的象征。name商标有助于您提升产品附加值、加入中高端市场，从而广受到消费者欢迎，把握市场动向的牛商标。",
    "name商标是第brandTypeCode类商标，图形的设计风格。是使用在useDescn的特征。一个牛的商标是企业成功的基石，商标图案是产品形象品质的象征是走向市场的第一步。name商标有助于您打开市场，受到消费者欢迎。牛商标——成就一个企业，推动一个行业。")

  def validStatusSelectValues = {
    val status = BrandStatus.values.toList.map(v => (v.id.toString, v.toString))
    ("all", "所有状态") :: status
  }

  def validOrderBySelectValues = {
    SearchBrandFormHelpers.adminBrderTypes
  }

  def validBrandTypeSelectValues = {
    val types = WebCacheHelper.brandTypes.values.toList.map(v => (v.code.toString, v.name.display()))
    ("all", "所有类别") :: types
  }

  def pageUrl(pageType: Int = 0, brandTypeCode: Int = 0, orderType: Int = 0) = {
    val url = "/market/" + pageType + "/" + brandTypeCode + "/" + orderType
    url
  }

}
