package code.lib

import com.tristanhunt.knockoff.DefaultDiscounter._
import com.tristanhunt.knockoff._
import scala.util.Try
import scala.util.Failure
import scala.util.Success

object TestMarkdown extends App {
  /*val markdown = """# I'm the *title*
     |
     | And I'm a paragraph"""

  val blocks = knockoff(markdown)

  val headers = blocks.filter(_.isInstanceOf[Header])

  println(blocks)
  println(toText(headers))
  println(toXHTML(headers))*/

  val receivers = List[String]("12233", "2323", "sdfsf")
  val ts = receivers.partition(id => Try { id.toInt }.isSuccess)
  println(ts + "===================")
}