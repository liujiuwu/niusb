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
case class HyNews(id: Int, title: String, content: String, classid: Int, HeadMessage: String)

object SyncDataWenda extends App {
  implicit val getWenwenResult = GetResult(r => Wenwen(r.nextInt(), r.nextString(), r.nextInt()))
  implicit val getHyNewsResult = GetResult(r => HyNews(r.nextInt(), r.nextString(), r.nextString(), r.nextInt(), r.nextString()))
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

  lazy val re1 = ("""szsbpp.com|haotm.cn|haotm.com|jsjb.org|hw-tm.com|jpsb.cn""", "niusb.com")
  lazy val re2 = ("""好标网""", "牛标网")
  lazy val re3 = ("""好标""", "牛标")
  lazy val re4 = ("""好商标""", "牛商标")
  lazy val re5 = ("""61559900|82826837""", "97145222")
  lazy val re6 = ("""义乌博锐知识产权代理有限公司""", "深圳牛标知识产权代理有限公司")
  lazy val re7 = ("""义乌""", "深圳")
  lazy val re8 = ("""zhcsb.aspx|news.aspx?id=770""", "")
  lazy val res = List(re1, re2, re3, re4, re5, re6, re7, re8)

  def syncWenwen(limit: Int) = {

    val sql = """SELECT id,content,classid FROM `wenwen` where  hf=0 """
    db.withSession {
      var syncNum = 0
      val startTime = System.currentTimeMillis()
      Q.queryNA[Wenwen](sql) foreach { wenwen =>
        val replySql = s"""SELECT id,hfcontent,classid FROM `wenwen` where hf=1 and hfid=${wenwen.id} limit 1"""
        Q.queryNA[Wenwen](replySql) foreach { reply =>
          val wenda = Wenda.create
          val realContent = {
            var newContent = wenwen.content
            for (re <- res) {
              newContent = newContent.replaceAll(re._1, re._2)
            }
            newContent
          }

          if (realContent.length() < 100) {

            wenda.title(realContent)
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
            wenda.content(realContent)
            wenda.replyCount(1)
            wenda.save

            val wendaType = WendaType.find(By(WendaType.code, wendaTypeCode))
            wendaType match {
              case Full(w) => w.wendaCount.incr()
              case _ =>
            }

            val realReplyContent = {
              var newContent = reply.content
              for (re <- res) {
                newContent = newContent.replaceAll(re._1, re._2)
              }
              newContent
            }

            val wendaReply = WendaReply.create
            wendaReply.wenda(wenda.id.is)
            wendaReply.content(realReplyContent)
            wendaReply.isRecommend(true)
            wendaReply.reply(0)
            wendaReply.save
          }
        }
      }
    }
  }

  def syncNews(limit: Int) = {
    val sql = """SELECT id,title,TContent,classid,HeadMessage FROM `hy_news` """
    db.withSession {
      var syncNum = 0
      val startTime = System.currentTimeMillis()
      Q.queryNA[HyNews](sql) foreach { news =>
        if (news.content != null && !news.content.trim.isEmpty()) {
          if (List(100101, 100102, 100103, 100104).exists(_ == news.classid)) {
            val article = Article.create
            article.title(news.title)
            article.downResources(news.HeadMessage)
            val realContent = {
              var newContent = news.content
              for (re <- res) {
                newContent = newContent.replaceAll(re._1, re._2)
              }
              newContent
            }

            article.content(realContent)
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
}