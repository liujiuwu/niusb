package code.lib

import java.text.SimpleDateFormat

import scala.slick.driver.MySQLDriver.simple.Database
import scala.slick.driver.MySQLDriver.simple.Database.threadLocalSession
import scala.slick.jdbc.GetResult
import scala.slick.jdbc.{ StaticQuery => Q }

import code.model.Article
import code.model.ArticleType
import code.model.MyDBVendor
import code.model.User
import code.model.Wenda
import code.model.WendaReply
import code.model.WendaType
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.db.DB
import net.liftweb.db.DB1.db1ToDb
import net.liftweb.db.DefaultConnectionIdentifier
import net.liftweb.http.LiftSession
import net.liftweb.http.S
import net.liftweb.mapper.By
import net.liftweb.mapper.Schemifier
import net.liftweb.util.StringHelpers

case class Wenwen(id: Int, content: String, classid: Int)
case class HyNews(id: Int, title: String, content: String, classid: Int)

object SyncDataWenda extends App {
  implicit val getWenwenResult = GetResult(r => Wenwen(r.nextInt(), r.nextString(), r.nextInt()))
  implicit val getHyNewsResult = GetResult(r => HyNews(r.nextInt(), r.nextString(), r.nextString(), r.nextInt()))
  DB.defineConnectionManager(DefaultConnectionIdentifier, MyDBVendor)
  Schemifier.schemify(true, Schemifier.infoF _, User, WendaType, Wenda, WendaReply, Article)
  lazy val db = Database.forURL("jdbc:mysql://localhost:3306/new_haotm", "ppseaer", "ppseaer@ppsea.com", driver = "com.mysql.jdbc.Driver")
  lazy val sdf = new SimpleDateFormat("yyyy-M-dd")
  lazy val session: LiftSession = new LiftSession("", StringHelpers.randomString(20), Empty)

  S.initIfUninitted(session)(init())
  def init() {
    //syncWenwen(-1)
    syncNews(-1)
  }

  def syncWenwen(limit: Int) = {
    val restr = """http://www.haotm.cn|www.haotm.com|haotm.cn|haotm.cn|http|好标网|标网"""
    val sql = """SELECT id,content,classid FROM `wenwen` where status=1 and hf=0 """ + (if (limit > 0) " limit " + limit else "")
    db.withSession {
      var syncNum = 0
      val startTime = System.currentTimeMillis()
      Q.queryNA[Wenwen](sql) foreach { wenwen =>
        val replySql = s"""SELECT id,hfcontent,classid FROM `wenwen` where hf=1 and hfid=${wenwen.id} limit 1"""
        Q.queryNA[Wenwen](replySql) foreach { reply =>
          val wenda = Wenda.create
          val title = wenwen.content.replaceAll(restr, "")
          if (title.indexOf("义乌") == -1) {
            wenda.title(title)
            wenda.asker(0)
            val wendaTypeCode = wenwen.classid match {
              case 2 => 2
              case 3 => 0
              case 4 => 3
              case 5 => 1
              case 6 => 4
              case 7 => 5
              case 8 => 6
              case 9 => 7
              case 10 => 8
              case 11 => 9
            }
            wenda.wendaTypeCode(wendaTypeCode)
            wenda.content(title)
            wenda.replyCount(1)
            wenda.save

            val wendaType = WendaType.find(By(WendaType.code, wendaTypeCode))
            wendaType match {
              case Full(w) => w.wendaCount.incr()
              case _ =>
            }

            val wendaReply = WendaReply.create
            wendaReply.wenda(wenda.id.is)
            wendaReply.content(reply.content.replaceAll(restr, ""))
            wendaReply.isRecommend(true)
            wendaReply.reply(0)
            wendaReply.save
          }
        }
      }
    }
  }

  def syncNews(limit: Int) = {
    val sql = """SELECT id,title,TContent,classid FROM `hy_news` where status=1 """ + (if (limit > 0) " limit " + limit else "")
    db.withSession {
      var syncNum = 0
      val startTime = System.currentTimeMillis()
      Q.queryNA[HyNews](sql) foreach { news =>
        if (List(100101, 100102, 100103, 100104).exists(_ == news.classid)) {
          val article = Article.create
          article.title(news.title)
          article.content(news.content)
          val articleType = news.classid match {
            case 100101 => ArticleType.News
            case 100103 => ArticleType.Laws
            case 100104 => ArticleType.ResourceDown
            case 100102 => ArticleType.Knowledge
          }
          article.articleType(articleType)
          article.save()
        }
      }
    }
  }
}