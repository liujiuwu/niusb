package code.lib

import java.io.File
import java.sql.Date
import java.text.SimpleDateFormat
import scala.slick.driver.MySQLDriver.simple.Database
import scala.slick.driver.MySQLDriver.simple.Database.threadLocalSession
import scala.slick.jdbc.GetResult
import scala.slick.jdbc.{ StaticQuery => Q }
import com.niusb.util.UploadHelpers
import com.niusb.util.WebHelpers
import com.sksamuel.scrimage.Format
import com.sksamuel.scrimage.Image
import code.model.Brand
import code.model.BrandType
import code.model.MyDBVendor
import code.model.User
import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.db.DB
import net.liftweb.db.DB1.db1ToDb
import net.liftweb.db.DefaultConnectionIdentifier
import net.liftweb.http.LiftSession
import net.liftweb.mapper.Schemifier
import net.liftweb.util.Helpers
import net.liftweb.util.StringHelpers
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import net.liftweb.mapper.By
import net.liftweb.http.S

case class Fbtm(id: Int, number: String, name: String, sbsm: String)
case class Kehu(id: Int, name: String, tel: String, tel1: String, qq: String, bz: String, indate: Date, sqname: String)
case class Trademark(id: Int, name: String, pic: String, indate: Date, price: Double, range: String, category: Int, number: String, regdate: String, sell: Boolean, address: String, tel: String, fax: String, coname: String, email: String, lsqz: String, kehu_1id: Int)
case class TrademarkCount(count: Int)

object SyncData extends App {
  implicit val getTrademarkResult = GetResult(r => Trademark(r.nextInt, r.nextString, r.nextString, r.nextDate(), r.nextDouble(), r.nextString(), r.nextInt(), r.nextString(), r.nextString(), r.nextBoolean(), r.nextString(), r.nextString(), r.nextString(), r.nextString(), r.nextString(), r.nextString(), r.nextInt()))
  implicit val getKehuResult = GetResult(r => Kehu(r.nextInt, r.nextString, r.nextString, r.nextString(), r.nextString(), r.nextString(), r.nextDate(), r.nextString()))
  implicit val getTrademarkCountResult = GetResult(r => TrademarkCount(r.nextInt()))

  DB.defineConnectionManager(DefaultConnectionIdentifier, MyDBVendor)
  Schemifier.schemify(true, Schemifier.infoF _, User, Brand, BrandType)
  lazy val db = Database.forURL("jdbc:mysql://localhost:3306/new_haotm", "ppseaer", "ppseaer@ppsea.com", driver = "com.mysql.jdbc.Driver")
  lazy val session: LiftSession = new LiftSession("", StringHelpers.randomString(20), Empty)
  S.initIfUninitted(session)(init())

  def init() {
    syncKehu("""e:\1\new_haotm""", """d:\new_haotm""", -1)
  }

  lazy val sdf = new SimpleDateFormat("yyyy-M-dd")
  def syncTrademark(user: User, kehuId: Int, sdir: String, ddir: String, limit: Int) = {
    val sql = s"""
      |select 
      | id,name,pic,indate,price,`range`,`category`,`number`,
      |regdate,sell,address,tel,fax,coname,email,lsqz,kehu_1id 
      | from trademark 
      | where kehu_1id=${kehuId} and sell=0 and del=0 and regdate!='' and LENGTH(kehu_1id)>0 and LENGTH(name)>0 and pic like '%/2011/%' 
      """ + (if (limit > 0) " limit " + limit else "")
    db.withSession {
      var syncNum = 0
      val startTime = System.currentTimeMillis()
      Q.queryNA[Trademark](sql.stripMargin) foreach { t =>
        handleImg(sdir + t.pic, ddir) match {
          case Full(newPicName) =>
            val brand = Brand.create
            brand.name(t.name.trim())
            brand.pic(newPicName)
            brand.basePrice((t.price * 10000).toInt)
            brand.useDescn(t.range.trim())
            brand.brandTypeCode(t.category)
            brand.regNo(t.number)
            if (t.regdate != null) {
              Try { sdf.parse(t.regdate) } match {
                case Success(regdate) => brand.regDate(regdate)
                case Failure(ex) => println(ex)
              }
            }
            brand.owner(user.id.is)
            brand.lsqz(t.lsqz.trim())
            brand.save()

            BrandType.find(By(BrandType.code, brand.brandTypeCode.is)) match {
              case Full(brandType) => brandType.brandCount.incr()
              case _ =>
            }
            syncNum += 1
          //println(t.id);
          case _ => println(t.id + "|no pic")
        }
      }
      println("sync brand " + syncNum + "|" + (System.currentTimeMillis() - startTime) + "ms")
    }
  }

  def handleImg(picPath: String, dir: String): Box[String] = {
    val imgFile = new File(picPath)
    if (imgFile.isFile() && imgFile.exists()) {
      val oImg = Image(imgFile)
      val newFileName = UploadHelpers.genNewFileName()
      val destPic = new File(UploadHelpers.uploadBrandDir(Full(dir)) + File.separator + newFileName)
      oImg.scaleTo(320, 200).writer(Format.JPEG).withProgressive(true).withCompression(80).write(destPic)
      Full(newFileName)
    } else {
      Empty
    }
  }

  def syncKehu(sdir: String, ddir: String, limit: Int) = {
    val user1 = User.create
    user1.name("刘久武")
    user1.mobile("13826526941")
    user1.password("pure2012!@#")
    user1.superUser(true)
    user1.save

    val user2 = User.create
    user2.name("黄伟")
    user2.mobile("18922831800")
    user2.password("huangwei2013!@#")
    user2.superUser(true)
    user2.save

    db.withSession {
      var syncNum = 0
      val startTime = System.currentTimeMillis()
      Q.queryNA[Kehu]("select id,name,tel,tel1,qq,bz,indate,sqname,count(distinct(tel))as c from kehu_1 where id>10 and LENGTH(name)>0 and LENGTH(tel)>0 GROUP BY tel " + (if (limit > 0) " limit " + limit else "")) foreach { u =>
        Option(u.tel) match {
          case Some(tel) =>
            val ucount = Q.queryNA[TrademarkCount](s"select count(id) from trademark where regdate!='' and kehu_1id=${u.id} and pic like '%/2011/%'").first().count
            if (ucount > 0) {
              val tels = tel.split(",")
              val regTel = tels(0).replaceAll("""\s|-""", "")
              WebHelpers.realMobile(Full(regTel)) match {
                case Full(mobile) =>
                  val user = User.create
                  user.mobile(mobile)
                  user.phone(if (u.tel1 != null && !u.tel1.trim.isEmpty()) u.tel1 + "," + tels.mkString(",") else tels.mkString(","))
                  user.name(u.name)
                  Option(u.qq) match {
                    case Some(qq) if (qq.contains("@")) => user.email(qq)
                    case Some(qq) => user.qq(qq)
                    case _ =>
                  }
                  user.password(Helpers.randomString(6))
                  user.descn(u.sqname)
                  user.remark(u.bz)
                  user.save
                  syncTrademark(user, u.id, sdir, ddir, -1)
                  syncNum += 1
                case _ => println("Tel error:" + u.id + "=" + tel)
              }
            } else {
              println(u.id + "|" + ucount)
            }
          case _ => println("No tel:" + u.id)
        }
      }
      println("sync kehu " + syncNum + "|" + (System.currentTimeMillis() - startTime) + "ms")

    }
  }
}