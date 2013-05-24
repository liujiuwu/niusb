package code.lib

import scala.io.Source

object SearchHelper extends App {
  val html = Source.fromURL("http://www.baidu.com")
  val s = html.mkString
  println(s)
}