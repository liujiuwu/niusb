package code.lib

import java.io.File
import java.sql.Date
import scala.slick.driver.MySQLDriver.simple.Database
import scala.slick.driver.MySQLDriver.simple.Database.threadLocalSession
import scala.slick.jdbc.GetResult
import scala.slick.jdbc.{ StaticQuery => Q }
import com.sksamuel.scrimage.Image
import code.model.Brand
import code.model.User
import code.rest.UploadManager
import net.liftweb.common.Full
import com.sksamuel.scrimage.Format
import net.liftweb.mapper.Schemifier
import net.liftweb.db.DB
import net.liftweb.db.DefaultConnectionIdentifier
import code.model.MyDBVendor
import net.liftweb.common.Box
import net.liftweb.common.Empty

case class Kehu(id: Int, name: String, tel: String, tel1: String, qq: String, bz: String, indate: Date, sqname: String)
case class Trademark(id: Int, name: String, pic: String, indate: Date, price: Double, range: String, category: Int, number: String, regdate: String, sell: Boolean, address: String, tel: String, fax: String, coname: String, email: String, lsqz: String, kehu_1id: Int)

object SyncData extends App {
  implicit val getTrademarkResult = GetResult(r => Trademark(r.nextInt, r.nextString, r.nextString, r.nextDate(), r.nextDouble(), r.nextString(), r.nextInt(), r.nextString(), r.nextString(), r.nextBoolean(), r.nextString(), r.nextString(), r.nextString(), r.nextString(), r.nextString(), r.nextString(), r.nextInt()))
  implicit val getKehuResult = GetResult(r => Kehu(r.nextInt, r.nextString, r.nextString, r.nextString(), r.nextString(), r.nextString(), r.nextDate(), r.nextString()))

  DB.defineConnectionManager(DefaultConnectionIdentifier, MyDBVendor)
  Schemifier.schemify(true, Schemifier.infoF _, User, Brand)

  val db = Database.forURL("jdbc:mysql://localhost:3306/haotm", "ppseaer", "ppseaer@ppsea.com", driver = "com.mysql.jdbc.Driver")
  //syncKehu()
  syncTrademark()

  //handleImg("""10782083.jpg""", """d:\tmp""")

  def syncTrademark(limit: Int = 1000) = {
    val sql = s"""
      |select 
      | id,name,pic,indate,price,range,category,number,
      |regdate,sell,address,tel,fax,coname,email,lsqz,kehu_1id 
      | from trademark 
      | where del=0 and LENGTH(kehu_1id)>0
      | limit ${limit}
      """

    db.withSession {
      var syncNum = 0
      val startTime = System.currentTimeMillis()
      Q.queryNA[Trademark](sql.stripMargin) foreach { t =>
        User.findByKey(t.kehu_1id) match {
          case Full(user) =>
            handleImg("""E:\1\haotm\""" + t.pic) match {
              case Full(newPicName) =>
                val brand = Brand.create
                brand.name(t.name)
                brand.pic(newPicName)
                brand.basePrice((t.price * 10000).toInt)
                brand.useDescn(t.range)
                brand.brandTypeId(t.category)
                brand.regNo(t.number)
                //brand.regDate(t.regdate)
                brand.owner(user.id.get)
                brand.lsqz(t.lsqz)
                brand.save()
                syncNum += 1
                println(t.id);
              case _ => println(t.id+"=没商标图")
            }
          case _ => println(t.id+"=没联系人")
        }
      }
      println("sync kehu " + syncNum + "|" + (System.currentTimeMillis() - startTime) + "ms")
    }
  }

  def handleImg(picPath: String, dir: String = """d:\haotm\"""): Box[String] = {
    val imgFile = new File(picPath)
    if (imgFile.isFile() && imgFile.exists()) {
      val oImg = Image(imgFile)
      val newFileName = UploadManager.genNewFileName()
      val destPic = new File(UploadManager.uploadBrandDir(Full(dir)) + File.separator + newFileName)
      //UploadManager.myFit(oImg, (320, 200), (oImg.width, oImg.height)).writer(Format.JPEG).withCompression(50).write(destPic)
      //UploadManager.myFit(oImg, (320, 200), (oImg.width, oImg.height)).write(destPic)
      oImg.scaleTo(320, 200).writer(Format.JPEG).withProgressive(true).withCompression(80).write(destPic)
      Full(newFileName)
    } else {
      Empty
    }
  }

  def syncKehu(limit: Int = 2000) = {
    db.withSession {
      var syncNum = 0
      val startTime = System.currentTimeMillis()
      Q.queryNA[Kehu]("select * from kehu_1 limit " + limit) foreach { u =>
        Option(u.tel) match {
          case Some(tel) =>
            val tels = tel.split(",")
            val regTel = tels(0).replaceAll("""\s|-""", "")
            WebHelper.realMobile(Full(regTel)) match {
              case Full(mobile) =>
                val user = User.create
                user.srcId(u.id)
                user.mobile(mobile)
                user.phone(if (u.tel1 != null && !u.tel1.isEmpty()) u.tel1 + "," + tels.mkString(",") else tels.mkString(","))
                user.name(u.name)
                Option(u.qq) match {
                  case Some(qq) if (qq.contains("@")) => user.email(qq)
                  case Some(qq) => user.qq(qq)
                  case _ =>
                }
                user.descn(u.sqname)
                user.remark(u.bz)
                user.save
                syncNum += 1
              case _ => println(u.id + "=" + tel)
            }
          case _ => println(u.id)
        }
      }
      println("sync kehu " + syncNum + "|" + (System.currentTimeMillis() - startTime) + "ms")
    }
  }
}