package code.lib

import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
import Q.interpolation
import java.sql.Date

object FirstExample extends App {

  case class Trademark(id: Int, name: String, pic: String, indate: Date, price: Double, range: String, category: Int, number: String, regdate: String, sell: Boolean, address: String, tel: String, fax: String, coname: String, email: String, sellindate: Date, lsqz: String, kehu_1id: Int)
  /*object Trademark extends Table[St]("trademark") {
    def id = column[Int]("id", O.PrimaryKey) // This is the primary key column
    def name = column[String]("name")
    def pic = column[String]("pic")
    def indate = column[Date]("indate")
    def price = column[Double]("price")
    def range = column[String]("range")
    def category = column[Int]("category")
    def content = column[String]("content")
    def number = column[String]("number")
    def regdate = column[String]("regdate")
    def tj = column[Boolean]("tj")
    def visits = column[Int]("visits")
    def pno = column[String]("pno")
    def newbz = column[Boolean]("newbz")
    def sell = column[Boolean]("sell")
    def people = column[String]("people")
    def categorytxt = column[String]("categorytxt")
    def web = column[String]("web")
    def ren = column[String]("ren")
    def address = column[String]("address")
    def tel = column[String]("tel")
    def fax = column[String]("fax")
    def coname = column[String]("coname")
    def email = column[String]("email")
    def xksb = column[Boolean]("xksb")
    def idp = column[Int]("idp")
    def sellindate = column[Date]("sellindate")
    def zysb = column[Boolean]("zysb")
    def lsqz = column[String]("lsqz")
    def del = column[Boolean]("del")
    def kehu_1id = column[Int]("kehu_1id")
    // Every table needs a * projection with the same type as the table's type parameter
    def * = id ~ name ~ pic ~ indate ~ price ~ range ~ category ~ content ~ number ~ regdate ~ tj ~ visits ~ pno ~ newbz ~ sell ~ people ~ categorytxt ~ web ~ ren ~ address ~ tel ~ fax ~ coname ~ email ~ xksb ~ idp ~ sellindate ~ zysb ~ lsqz ~ del ~ kehu_1id
  }*/

  implicit val getTrademarkResult = GetResult(r => Trademark(r.nextInt,r.nextString,r.nextString,r.nextDate(),r.nextDouble(),r.nextString(),r.nextInt(),r.nextString(),r.nextString(),r.nextBoolean(),r.nextString(),r.nextString(),r.nextString(),r.nextString(),r.nextString(),r.nextDate(),r.nextString(),r.nextInt()))

  val db = Database.forURL("jdbc:mysql://localhost:3306/haotm", "ppseaer", "ppseaer@ppsea.com", driver = "com.mysql.jdbc.Driver")
  db.withSession {
    Q.queryNA[Trademark]("select * from trademark order by id desc limit 10") foreach { c =>
      println("  " + c.id + "\t" + c.name + "\t" + c.pic + "\t" + c.indate)
    }
  }

}