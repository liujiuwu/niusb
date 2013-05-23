package code.lib

import code.model.MyDBVendor
import code.model.User
import net.liftweb.db.DB
import net.liftweb.db.DefaultConnectionIdentifier
import net.liftweb.mapper.Schemifier
import net.liftweb.http.S

object Test extends App {

  def initDb = {
    DB.defineConnectionManager(DefaultConnectionIdentifier, MyDBVendor)
    Schemifier.schemify(true, Schemifier.infoF _, User)
  }

  //initDb

  /*val user = new User
  user.mobile("13826526941")
  user.qq("923933533")

  user.validate match {
    case Nil => user.save
    case _ =>
  }*/

  val mobile = "13826526941"
  val mobileRegx = """^(13[0-9]|15[0|3|6|7|8|9]|18[8|9])(\d{8})$""".r
  mobile match {
    case mobileRegx(m,n) => println(m+n)
    case _ => println("错误的手机号")
  }

}