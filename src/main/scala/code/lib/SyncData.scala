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

case class Fbtm(id: Int, number: String, name: String, sbsm: String)
case class Kehu(id: Int, name: String, tel: String, tel1: String, qq: String, bz: String, indate: Date, sqname: String)
case class Trademark(id: Int, name: String, pic: String, indate: Date, price: Double, range: String, category: Int, number: String, regdate: String, sell: Boolean, address: String, tel: String, fax: String, coname: String, email: String, lsqz: String, kehu_1id: Int)

object SyncData extends App {
  implicit val getTrademarkResult = GetResult(r => Trademark(r.nextInt, r.nextString, r.nextString, r.nextDate(), r.nextDouble(), r.nextString(), r.nextInt(), r.nextString(), r.nextString(), r.nextBoolean(), r.nextString(), r.nextString(), r.nextString(), r.nextString(), r.nextString(), r.nextString(), r.nextInt()))
  implicit val getKehuResult = GetResult(r => Kehu(r.nextInt, r.nextString, r.nextString, r.nextString(), r.nextString(), r.nextString(), r.nextDate(), r.nextString()))
  DB.defineConnectionManager(DefaultConnectionIdentifier, MyDBVendor)
  Schemifier.schemify(true, Schemifier.infoF _, User, Brand)
  lazy val db = Database.forURL("jdbc:mysql://localhost:3306/new_haotm", "ppseaer", "ppseaer@ppsea.com", driver = "com.mysql.jdbc.Driver")

  /*if (args.length < 3) {
    println("use source dir , dist dir and data limit .")
    System.exit(0)
  }

  init(args(0), args(1), args(2).toInt)*/
  init()
  def init() {
    //SyncData.init("/alidata/haotm", "/alidata/niusb_upload_file", 2000)
    //syncKehu("/alidata/haotm", "/alidata/niusb_upload_file", 2000)
    syncKehu("""e:\1\haotm""", """d:\haotm""", -1)
  }

  lazy val sdf = new SimpleDateFormat("yyyy-M-dd")
  def syncTrademark(sdir: String, ddir: String, limit: Int) = {
    val sql = """
      |select 
      | id,name,pic,indate,price,`range`,`category`,`number`,
      |regdate,sell,address,tel,fax,coname,email,lsqz,kehu_1id 
      | from trademark 
      | where sell=0 and del=0 and LENGTH(kehu_1id)>0 and LENGTH(name)>0 and pic like '%/2011/%' 
      """ + (if (limit > 0) " limit " + limit)

    db.withSession {
      var syncNum = 0
      val startTime = System.currentTimeMillis()
      Q.queryNA[Trademark](sql.stripMargin) foreach { t =>
        User.findByKey(t.kehu_1id) match {
          case Full(user) =>
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
                //brand.regDate(t.regdate)
                brand.owner(user.id.get)
                brand.lsqz(t.lsqz.trim())
                brand.save()
                syncNum += 1
              //println(t.id);
              case _ => println(t.id + "=没商标图")
            }
          case _ => println(t.id + "=没联系人")
        }
      }
      println("sync kehu " + syncNum + "|" + (System.currentTimeMillis() - startTime) + "ms")
    }
  }

  def handleImg(picPath: String, dir: String): Box[String] = {
    val imgFile = new File(picPath)
    if (imgFile.isFile() && imgFile.exists()) {
      val oImg = Image(imgFile)
      val newFileName = UploadHelpers.genNewFileName()
      val destPic = new File(UploadHelpers.uploadBrandDir(Full(dir)) + File.separator + newFileName)
      //UploadManager.myFit(oImg, (320, 200), (oImg.width, oImg.height)).writer(Format.JPEG).withCompression(50).write(destPic)
      //UploadManager.myFit(oImg, (320, 200), (oImg.width, oImg.height)).write(destPic)
      oImg.scaleTo(320, 200).writer(Format.JPEG).withProgressive(true).withCompression(80).write(destPic)
      Full(newFileName)
    } else {
      Empty
    }
  }

  def syncKehu(sdir: String = """E:\1\haotm\""", ddir: String = """d:\haotm\""", limit: Int = 2000) = {
    db.withSession {
      var syncNum = 0
      val startTime = System.currentTimeMillis()
      Q.queryNA[Kehu]("select id,name,tel,tel1,qq,bz,indate,sqname,count(distinct(tel))as c from kehu_1 where id>10 and LENGTH(name)>0 and LENGTH(tel)>0 GROUP BY tel " + (if (limit > 0) " limit " + limit else "")) foreach { u =>
        Option(u.tel) match {
          case Some(tel) =>
            val tels = tel.split(",")
            val regTel = tels(0).replaceAll("""\s|-""", "")
            println(regTel+"==")
            WebHelpers.realMobile(Full(regTel)) match {
              case Full(mobile) =>
               // println(u.name+"|"+mobile)
               /* val user = User.create
                user.srcId(u.id)
                user.mobile(mobile)
                user.phone(if (u.tel1 != null && !u.tel1.isEmpty()) u.tel1 + "," + tels.mkString(",") else tels.mkString(","))
                user.name(u.name)
                Option(u.qq) match {
                  case Some(qq) if (qq.contains("@")) => user.email(qq)
                  case Some(qq) => user.qq(qq)
                  case _ =>
                }
                user.password(Helpers.randomString(6))
                user.descn(u.sqname)
                user.remark(u.bz)
                user.save*/
                syncNum += 1
              case _ => println(u.id + "=" + tel)
            }
          case _ => println(u.id)
        }
      }
      println("sync kehu " + syncNum + "|" + (System.currentTimeMillis() - startTime) + "ms")
      //syncTrademark(sdir, ddir, limit)
    }
  }
}