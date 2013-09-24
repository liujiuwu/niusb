package niusb

import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.WebDriver
import org.openqa.selenium.By
import org.openqa.selenium.By.ByTagName
import net.liftweb.common.Loggable
import com.gargoylesoftware.htmlunit.javascript.configuration.WebBrowser
import org.scalatest.matchers.Helper
import net.liftweb.util.Helpers

object SearchBrandTest extends App {
 /* val webDriver: WebDriver = new HtmlUnitDriver
  val regno = "5536534"
  val home = """http://sbcx.saic.gov.cn/trade/"""
  val regnoUrl = s"""http://sbcx.saic.gov.cn/trade/servlet?Search=FL_REG_List&SelectContent="注册号是：${regno}"&RegNO=${regno}"""
  webDriver.get(regnoUrl)

  println(webDriver.getPageSource()+"====");*/

  //val ele = TagNameQuery("tr")

  //val tdEles = webDriver.findElements(By.xpath("//form/table/tbody/tr[3]/td"))
 // val (brandType, brandName) = (tdEles.get(2).getText(), tdEles.get(3).getText())
  //println(brandType + brandName)
  //val dataLine = XPathQuery("//form/table/tbody/tr[3]").findElement
  //println(dataLine.get)
  /*val dataLine = webDriver.findElement(By.ByTagName)*/
  //println(webDriver.findElement(By.))
  
  val test="12"
    println(Helpers.asInt(test))
}