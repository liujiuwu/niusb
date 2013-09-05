package niusb

import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.WebDriver
import org.openqa.selenium.By
import org.openqa.selenium.By.ByTagName
import net.liftweb.common.Loggable
import com.gargoylesoftware.htmlunit.javascript.configuration.WebBrowser

object SearchBrandTest extends App {
  val webDriver: WebDriver = new HtmlUnitDriver
  val regno = "8274592"
  val home = """http://sbcx.saic.gov.cn/trade/"""
  val regnoUrl = s"""http://sbcx.saic.gov.cn/trade/servlet?Search=FL_REG_List&RegNO=${regno}"""
  webDriver.get(regnoUrl)

  println(webDriver.getPageSource());

  //val ele = TagNameQuery("tr")

  val tdEles = webDriver.findElements(By.xpath("//form/table/tbody/tr[3]/td"))
  val (brandType, brandName) = (tdEles.get(2).getText(), tdEles.get(3).getText())
  println(brandType + brandName)
  //val dataLine = XPathQuery("//form/table/tbody/tr[3]").findElement
  //println(dataLine.get)
  /*val dataLine = webDriver.findElement(By.ByTagName)*/
  //println(webDriver.findElement(By.))
}