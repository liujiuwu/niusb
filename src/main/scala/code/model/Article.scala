package code.model

import java.text.SimpleDateFormat
import net.liftweb.common._
import net.liftweb.mapper._
import com.niusb.util.WebHelpers
import com.niusb.util.MemHelpers
import net.liftweb.util.Helpers._
import scala.xml.Unparsed
import scala.xml.Text

object ArticleType extends Enumeration {
  type ArticleType = Value
  val Notice = Value(0, "公告")
  val News = Value(1, "新闻")
  val Help = Value(2, "帮助")
  val Knowledge = Value(3, "商标知识")
  val Laws = Value(4, "政策法规")
}

object ArticleStatus extends Enumeration {
  type ArticleStatus = Value
  val Normal = Value(0, "正常")
  val Close = Value(1, "关闭")
}

class Article extends LongKeyedMapper[Article] with CreatedUpdated with IdPK {
  def getSingleton = Article
  object title extends MappedString(this, 100) {
    def displayTitle(isLink: Boolean = true, linkUrl: String = "/news/view/") = {
      if (isLink) {
        <a href={ linkUrl + id.get } title={ this.get } target="_blank" style={ if (isRecommend.is) "color:#EE472C" else null }>{ this.get }</a>
      } else {
        <span style={ if (isRecommend.is) "color:#EE472C" else null }>{ this.get }</span>
      }
    }
  }

  object articleType extends MappedEnum(this, ArticleType) {
    override def defaultValue = ArticleType.News
    override def dbColumnName = "article_type"
  }
  object articleFrom extends MappedString(this, 300) {
    override def dbColumnName = "article_from"
    def displayFrom = {
      Unparsed(
        if (this.get == null || this.get.trim.isEmpty()) {
          s"""<a href="${WebHelpers.WebSiteUrlAndName._1}" target="_blank">${WebHelpers.WebSiteUrlAndName._2}</a>"""
        } else {
          val fromLink = this.get.split(":", 2)
          if (fromLink.length >= 2) {
            s"""<a href="${fromLink(1)}" target="_blank">${fromLink(0)}</a>"""
          } else {
            this.get
          }
        })
    }
  }
  object content extends MappedText(this)
  object articleOrder extends MappedInt(this) {
    override def dbColumnName = "article_order"
  }

  object status extends MappedEnum(this, ArticleStatus) {
    override def defaultValue = ArticleStatus.Normal
  }

  object isRecommend extends MappedBoolean(this) { //是否推荐
    override def displayName = "推荐"
    override def defaultValue = false
    override def dbColumnName = "is_recommend"
    def displayRecommend = if (this.get) "是" else "否"
  }

  object readCount extends MappedInt(this) {
    override def defaultValue = 0
    override def dbColumnName = "read_count"
    def incr(ip: String): Int = {
      val key = WebHelpers.memKey(ip, "article", id.get.toString())
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

  override lazy val createdAt = new MyCreatedAt(this) {
    override def dbColumnName = "created_at"
    override def format(date: java.util.Date): String = WebHelpers.fmtDateStr(date, WebHelpers.dfLongTime)
  }

  override lazy val updatedAt = new MyUpdatedAt(this) {
    override def dbColumnName = "updated_at"
    override def format(date: java.util.Date): String = WebHelpers.fmtDateStr(date, WebHelpers.dfLongTime)
  }

  def shortCreatedAt = WebHelpers.fmtDateStr(createdAt.get, WebHelpers.df)
}

object Article extends Article with CRUDify[Long, Article] with Paginator[Article] {
  override def dbTableName = "articles"
  override def fieldOrder = List(id, title, articleType, articleFrom, readCount, content, createdAt, updatedAt)

  def validArticleTypeSelectValues = {
    val articleTypes = ArticleType.values.toList.map(v => (v.id.toString, v.toString))
    ("all", "所有类型") :: articleTypes
  }
  def validStatusSelectValues = {
    val status = ArticleStatus.values.toList.map(v => (v.id.toString, v.toString))
    ("all", "所有状态") :: status
  }
}
