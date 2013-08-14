package code.model

import java.text.SimpleDateFormat

import code.lib.WebHelper
import net.liftweb.common._
import net.liftweb.mapper._

object ArticleType extends Enumeration {
  type ArticleType = Value
  val System = Value(0, "维护公告")
  val News = Value(1, "新闻")
  val Help = Value(2, "帮助")
}

object ArticleStatus extends Enumeration {
  type ArticleStatus = Value
  val Normal = Value(0, "正常")
  val Close = Value(1, "关闭")
}

class Article extends LongKeyedMapper[Article] with CreatedUpdated with IdPK {
  def getSingleton = Article
  object title extends MappedString(this, 100)
  object articleType extends MappedEnum(this, ArticleType) {
    override def defaultValue = ArticleType.News
    override def dbColumnName = "article_type"
  }
  object articleFrom extends MappedString(this, 50) {
    override def dbColumnName = "article_from"
  }
  object content extends MappedText(this)
  object articleOrder extends MappedInt(this) {
    override def dbColumnName = "article_order"
  }

  object status extends MappedEnum(this, ArticleStatus) {
    override def defaultValue = ArticleStatus.Normal
  }

  object readCount extends MappedInt(this) {
    override def dbColumnName = "read_count"
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
