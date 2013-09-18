package code.lib

import java.io.File
import java.sql.Date
import scala.slick.driver.MySQLDriver.simple.Database
import scala.slick.driver.MySQLDriver.simple.Database.threadLocalSession
import scala.slick.jdbc.GetResult
import scala.slick.jdbc.{ StaticQuery => Q }
import com.sksamuel.scrimage.Format
import com.sksamuel.scrimage.Image
import code.model.Brand
import code.model.MyDBVendor
import code.model.User
import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.db.DB
import net.liftweb.db.DB1.db1ToDb
import net.liftweb.db.DefaultConnectionIdentifier
import net.liftweb.mapper.Schemifier
import scala.util.Try
import java.text.SimpleDateFormat
import scala.util.Success
import scala.util.Failure
import net.liftweb.util.Helpers
import com.niusb.util.UploadHelpers
import com.niusb.util.WebHelpers
import code.model._
import net.liftweb.mapper.By

case class Wenwen(id: Int, content: String, classid: Int)

object SyncDataWenda extends App {
  implicit val getWenwenResult = GetResult(r => Wenwen(r.nextInt(), r.nextString(), r.nextInt()))
  DB.defineConnectionManager(DefaultConnectionIdentifier, MyDBVendor)
  Schemifier.schemify(true, Schemifier.infoF _, User, WendaType, Wenda, WendaReply)
  lazy val db = Database.forURL("jdbc:mysql://localhost:3306/new_haotm", "ppseaer", "ppseaer@ppsea.com", driver = "com.mysql.jdbc.Driver")
  lazy val sdf = new SimpleDateFormat("yyyy-M-dd")

  init()
  def init() {
    syncWenwen(-1)
  }

  def syncWenwen(limit: Int) = {
    val sql = """SELECT id,content,classid FROM `wenwen` where hf=0 """ + (if (limit > 0) " limit " + limit else "")
    db.withSession {
      var syncNum = 0
      val startTime = System.currentTimeMillis()
      Q.queryNA[Wenwen](sql) foreach { wenwen =>
        val replySql = s"""SELECT id,hfcontent,classid FROM `wenwen` where hf=1 and hfid=${wenwen.id} limit 1"""
        Q.queryNA[Wenwen](replySql) foreach { reply =>
          val wenda = Wenda.create
          val title = wenwen.content
          wenda.title(if (title.length() > 90) title.substring(0, 90) + " ..." else title)
          wenda.asker(1)
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
          wenda.content(wenwen.content)
          wenda.replyCount(1)
          wenda.save

          val wendaType = WendaType.find(By(WendaType.code, wendaTypeCode))
          wendaType match {
            case Full(w) => w.wendaCount.incr()
            case _ =>
          }

          val wendaReply = WendaReply.create
          wendaReply.wenda(wenda.id.is)
          wendaReply.content(reply.content)
          wendaReply.isRecommend(true)
          wendaReply.reply(1)
          wendaReply.save
        }
      }
    }
  }
}