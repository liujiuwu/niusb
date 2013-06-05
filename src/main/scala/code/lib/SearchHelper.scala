package code.lib

import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.By
import scala.collection.JavaConversions._
import sys.process._
import java.net.URL
import java.io.File
import net.liftweb.http.InMemoryResponse
import org.apache.commons.io.IOUtils
import net.liftweb.common.Box
import net.liftweb.http.LiftResponse
import net.liftweb.common.Full
import net.liftweb.common.Empty

object SearchHelper {

  def searchBrandByRegNo(regno: String): Map[String, String] = {
    if (regno == null) {
      return Map.empty
    }

    val home = """http://sbcx.saic.gov.cn/trade/"""
    val regnoUrl = s"""http://sbcx.saic.gov.cn/trade/servlet?Search=FL_REG_List&RegNO=${regno}"""

    val driver: WebDriver = new HtmlUnitDriver()
    driver.get(home)
    driver.get(regnoUrl)

    val brandTrs = driver.findElements(By.tagName("tr"))
    if (brandTrs.length < 4) {
      return Map.empty
    }
    val brandSimpleData = brandTrs(3).getText().split(" ")
    if (brandSimpleData.length < 4) {
      return Map.empty
    }

    val (brandType, brandName) = (brandSimpleData(2), brandSimpleData(3))
    val detailUrl = s"""http://sbcx.saic.gov.cn/trade/servlet?Search=TI_REG&RegNO=${regno}&IntCls=${brandType}&iYeCode=0"""
    val picUrl = s"""http://sbcx.saic.gov.cn/trade/pictureservlet?RegNO=${regno}&IntCls=${brandType}"""
    driver.get(detailUrl)

    var ds = List[String]()
    var datas = driver.findElements(By.tagName("table"))(0).getText().trim().replaceAll("商标的详细信息|查看详细信息 ...|商标图像|申请人名称\\(英文\\)|申请人地址\\(英文\\)", "").replace(" ／ ", "/").split("\n")
    datas.zipWithIndex.foreach {
      case (data, i) =>
        if (i <= 1 && data.trim() != "") {
          ds = ds ::: List(data.trim())
        }
    }

    val dataStr = datas.mkString(" ")
    """类似群(.+)初审公告期号""".r.findFirstMatchIn(dataStr).groupBy(gs => {
      ds = ds ::: List(gs.group(1).trim().replaceAll("[\\s]+", ","))
    })

    """(初审公告期号.+)""".r.findFirstMatchIn(dataStr).groupBy(gs => {
      ds = ds ::: List(gs.group(1).trim())
    })

    var resultMap = Map[String, String]()
    resultMap += ("name" -> brandName)
    ds.zipWithIndex.foreach {
      case (data, rowIdx) =>
        val rowDatas = data.split("[\\s]+")
        if (rowIdx == 0) {
          resultMap += ("zch" -> rowDatas(1))
          resultMap += ("flh" -> rowDatas(3))
        } else if (rowIdx == 1) {
          resultMap += ("sqr" -> rowDatas(1))
          resultMap += ("sqrdz" -> rowDatas(3))
          resultMap += ("fwlb" -> rowDatas(5))
        } else if (rowIdx == 2) {
          resultMap += ("lsq" -> rowDatas(0))
        } else if (rowIdx == 3) {
          resultMap += ("zcggrq" -> rowDatas(7))
        }
    }
    //new URL(picUrl) #> new File("d:\\test.jpg") !!

    resultMap
  }

  def searchBrandPicByRegNo(regno: String): Box[LiftResponse] = {
    if (regno == null) {
      return Empty
    }

    val home = """http://sbcx.saic.gov.cn/trade/"""
    val regnoUrl = s"""http://sbcx.saic.gov.cn/trade/servlet?Search=FL_REG_List&RegNO=${regno}"""

    val driver: WebDriver = new HtmlUnitDriver()
    driver.get(home)
    driver.get(regnoUrl)

    val brandSimpleData = driver.findElements(By.tagName("tr"))(3).getText().split(" ")
    val (brandType, brandName) = (brandSimpleData(2), brandSimpleData(3))
    val picUrl = s"""http://sbcx.saic.gov.cn/trade/pictureservlet?RegNO=${regno}&IntCls=${brandType}"""
    driver.get(picUrl)

    println(driver.getPageSource())
    //val is = new URL(picUrl).openStream()

    val imageBytes = driver.getPageSource().getBytes()

    Full(InMemoryResponse(imageBytes, ("Content-Type" -> "image/jpeg") :: Nil, Nil, 200))
  }
}