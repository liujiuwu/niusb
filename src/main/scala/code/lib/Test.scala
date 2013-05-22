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

  val userUrl = """(^/user.*)""".r
  "/user/brand/adds" match {
    case userUrl(url) => println(url)
    case _ => println("**********")
  }

}