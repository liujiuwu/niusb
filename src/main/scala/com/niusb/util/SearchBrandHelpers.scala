package com.niusb.util

import java.io.FileOutputStream
import scala.collection.JavaConversions.asScalaBuffer
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import com.gargoylesoftware.htmlunit.UnexpectedPage
import com.gargoylesoftware.htmlunit.WebClient
import net.liftweb.actor.LiftActor
import net.liftweb.common.Full
import org.apache.commons.io.IOUtils

case class SearchBrandRegno(regno: String)
case class SearchBrandResult(regno: String, brandType: String, name: String, zwsqr: String, zwdz: String, ywsqr: String, ywdz: String, fwlb: String, lsqz: String, zcggrq: String)

trait SearchBrandByWebBrowser {
  val webDriver: WebDriver = new HtmlUnitDriver
  val baseUrl = "http://sbcx.saic.gov.cn/trade/servlet?"

  def search(regno: String): Option[SearchBrandResult] = {
    val regnoUrl = s"""${baseUrl}Search=FL_REG_List&RegNO=${regno}"""
    webDriver.get(regnoUrl)

    val pageSource = webDriver.getPageSource()
    if (pageSource.toString.isEmpty() || pageSource.toString.indexOf("系统正忙，请稍后再试！") != -1) {
      return None
    }

    val tds = webDriver.findElements(By.xpath("//form/table/tbody/tr[3]/td"))
    val (brandType, brandName) = (tds.get(2).getText, tds.get(3).getText)

    val detailUrl = s"""${baseUrl}Search=TI_REG&RegNO=${regno}&IntCls=${brandType}&iYeCode=0"""
    webDriver.get(detailUrl)

    val table = webDriver.findElements(By.xpath("/html/body/table//table")).head
    val tr3Data = table.findElements(By.xpath("//tr[3]/td"))
    val (zwSqr, zwDz) = (tr3Data(1).getText.trim, tr3Data(3).getText.trim)

    val tr4Data = table.findElements(By.xpath("//tr[4]/td"))
    val (ywSqr, ywDz) = (tr3Data(1).getText.trim, tr3Data(3).getText.trim)

    val table2Data = table.findElements(By.xpath("//tr[5]/td/table//td"))
    val fwlb = table2Data(3).getText.replaceAll("查看详细信息 ...|[\\s]+", "").trim
    val lsqz = table2Data(5).getText.replaceAll("<br/>|[\\s]+", " ").trim

    val tr7Data = table.findElements(By.xpath("//tr[7]/td"))
    val zcggrq = tr7Data(3).getText.replaceAll("年|月", "-").trim

    Some(SearchBrandResult(regno, brandType, brandName, zwSqr, zwDz, ywSqr, ywDz, fwlb, lsqz, zcggrq))
  }
}

object SearchBrandActor extends LiftActor with SearchBrandByWebBrowser {
  def messageHandler = {
    case SearchBrandRegno(regno: String) => {
      reply(search(regno))
    }
    case _ =>
  }
}

object SearchBrandHelpers extends SearchBrandByWebBrowser {
  def searchBrandByRegNo(regno: String): Option[SearchBrandResult] = {
    if (regno == null) {
      return None
    }

    val ret = SearchBrandActor !! (SearchBrandRegno(regno), 30000)
    ret match {
      case Full(r) => r.asInstanceOf[Option[SearchBrandResult]]
      case _ => None
    }
    //search(regno)
  }

  val ret = searchBrandByRegNo("10517930")
  println(ret)
}

object WebTest extends App {
  val webClient = new WebClient
  val page: UnexpectedPage = webClient.getPage("http://sbcx.saic.gov.cn/trade/pictureservlet?RegNO=10951328&IntCls=25")
  val input = page.getWebResponse().getContentAsStream()
  val destinationFile = "d://tmp//image.jpg"
  val os = new FileOutputStream(destinationFile)
  os.write(IOUtils.toByteArray(input))
  os.close()
  input.close()
}

