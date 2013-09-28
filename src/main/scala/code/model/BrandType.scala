package code.model

import scala.collection.mutable.LinkedHashMap
import scala.xml.NodeSeq
import scala.xml.Text
import code.lib.WebCacheHelper
import net.liftweb.mapper._
import scala.util.Try
import scala.util.Success
import net.liftweb.http.S
import net.liftweb.util.HttpHelpers
import net.liftweb.util.Helpers
import com.niusb.util.WebHelpers

class BrandType extends LongKeyedMapper[BrandType] with IdPK {
  def getSingleton = BrandType

  object code extends MappedInt(this) {
    override def dbIndexed_? = true
  }

  object name extends MappedString(this, 30) {
    def display(sp: String = "->") = {
      code.is.toString() + sp + this.is
    }

    def displayTypeName(keyword: String = "", isBrandCount: Boolean = true): NodeSeq = {
      val pageType = Try(S.param("pageType").openOr("0").toInt) match {
        case Success(code) => code
        case _ => 0
      }

      val brandTypeCode = Try(S.param("brandTypeCode").openOr("0").toInt) match {
        case Success(code) => code
        case _ => 0
      }

      val orderType = Try(S.param("orderType").openOr("0").toInt) match {
        case Success(code) => code
        case _ => 0
      }

      val oname = Text((if (code.is < 10) "0" + code.is else code.is) + "." + this.is + { if (isBrandCount) "(" + brandCount + ")" else "" })
      val dname =
        if (isRecommend.is) {
          <span style={ if (brandTypeCode != code.is) "color:#E13335;" else null }>{ oname }</span>
        } else {
          oname
        }

      val pageUrl = Helpers.appendParams(Brand.pageUrl(pageType, code.is, orderType), List("keyword" -> keyword))
      <a href={ pageUrl } class={ if (brandTypeCode == code.is) "active" else null } title={ descn.is } target={ if (isBrandCount) "_blank" else null }>{ dname }</a>
    }
  }

  object brandCount extends MappedInt(this) {
    override def dbColumnName = "brand_count"
    override def defaultValue = 0
    def incr(v: Int = 1) = {
      require(v > 0)
      this(this.is + v)
      save
    }

    def decr(v: Int = 1) = {
      require(v > 0)
      val nv = this.is - v
      this(if (nv < 0) 0 else nv)
      save
    }
  }

  object isRecommend extends MappedBoolean(this) { //是否推荐
    override def displayName = "推荐"
    override def defaultValue = false
    override def dbColumnName = "is_recommend"
    def displayRecommend = if (this.get) "是" else "否"
  }

  object descn extends MappedString(this, 600)
}

object BrandType extends BrandType with CRUDify[Long, BrandType] with LongKeyedMetaMapper[BrandType] {
  override def dbTableName = "brand_types"
  override def fieldOrder = List(id, code, name, brandCount, isRecommend, descn)
  def isBrandType(code: Int) = WebCacheHelper.brandTypes.contains(code)
}