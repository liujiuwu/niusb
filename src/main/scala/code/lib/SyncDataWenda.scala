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
import code.model.Wenda

case class Wenwen(id: Int, content: String, classid: Int)

object SyncDataWenda extends App {
  implicit val getWenwenResult = GetResult(r => Wenwen(r.nextInt(), r.nextString(), r.nextInt()))
  //DB.defineConnectionManager(DefaultConnectionIdentifier, MyDBVendor)
  //Schemifier.schemify(true, Schemifier.infoF _, User, Brand)
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
        val replySql = s"""SELECT id,content,classid FROM `wenwen` where hf=1 and hfid=${wenwen.id} limit 1"""
        Q.queryNA[Wenwen](replySql) foreach { reply =>
          val wenda = Wenda.create
          wenda.content(wenwen.content)
          wenda.readCount(1)
          println(wenwen + "|" + reply)
        }
      }
    }
  }
}