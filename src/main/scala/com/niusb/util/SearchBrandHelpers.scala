package com.niusb.util

import java.io.FileOutputStream
import scala.collection.JavaConversions.asScalaBuffer
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.http.InMemoryResponse
import net.liftweb.http.LiftResponse
import net.liftweb.actor.LiftActor
import org.scalatest.selenium.WebBrowser.XPathQuery
import scala.collection.mutable.LinkedHashSet
import org.scalatest.selenium.WebBrowser

case class SearchBrandRegno(regno: String)
case class SearchBrandResult(regno: String, brandType: String, name: String, zwsqr: String, zwdz: String, ywsqr: String, ywdz: String, fwlb: String, lsqz: String, zcggrq: String)

object SearchBrandActor extends LiftActor with WebBrowser {
  implicit val webDriver: WebDriver = new HtmlUnitDriver
  val baseUrl = "http://sbcx.saic.gov.cn/trade/servlet?"
  def messageHandler = {
    case SearchBrandRegno(regno: String) => {
      reply(search(regno))
    }
    case _ =>
  }

  private def search(regno: String): Option[SearchBrandResult] = {
    val regnoUrl = s"""${baseUrl}Search=FL_REG_List&RegNO=${regno}"""
    go to (regnoUrl)
    println(pageSource)

    if (pageSource.toString.isEmpty() || pageSource.toString.indexOf("系统正忙，请稍后再试！") != -1) {
      return None
    }

    val tds = (XPathQuery("//form/table/tbody/tr[3]/td").findAllElements).toList
    val (brandType, brandName, applicant) = (tds(2).text, tds(3).text, tds(4).text)

    val detailUrl = s"""${baseUrl}Search=TI_REG&RegNO=${regno}&IntCls=${brandType}&iYeCode=0"""
    go to (detailUrl)

    val trs = (XPathQuery("/html/body/table//table//tr").findAllElements).toList
    var datas = LinkedHashSet[String]()
    for (tr <- trs; trText = tr.text.replaceAll("商标的详细信息|商标图像|查看详细信息 ... |商标流程", "").trim.replace(" ／ ", "/"); if (!trText.isEmpty())) yield {
      datas += trText
    }

    var resultMap = Map[String, String]()
    datas.zipWithIndex.foreach {
      case (data, rowIdx) =>
        val rowDatas = data.split("[\\s]+")
        if (rowIdx == 1) { //申请人名称(中文) 法国拉菲尔(国际)品牌管理发展有限公司  申请人地址(中文) 香港湾仔轩尼诗道289-295号朱钧记商业中心16楼B室
          val lineDatas = data.split("""申请人地址\(中文\)""")
          resultMap += ("zwsqr" -> lineDatas(0).split("""申请人名称\(中文\)""")(1))
          resultMap += ("zwdz" -> lineDatas(1))
        } else if (rowIdx == 2) {
          val lineDatas = data.split("""申请人地址\(英文\)""")
          resultMap += ("ywsqr" -> lineDatas(0).split("""申请人名称\(英文\)""")(1))
          resultMap += ("ywdz" -> lineDatas(1))
        } else if (rowIdx == 3) { //[商品/服务列表,皮带(服饰用);,类似群,2512]
          resultMap += ("fwlb" -> rowDatas(1))
          resultMap += ("lsqz" -> rowDatas(3))
        } else if (rowIdx == 5) { //[初审公告日期,2012-10-06,注册公告日期,2013-01-07]
          resultMap += ("zcggrq" -> rowDatas(3))
        }
    }

    val ret = SearchBrandResult(regno, brandType, brandName, resultMap("zwsqr"), resultMap("zwdz"), resultMap("ywsqr"), resultMap("ywdz"), resultMap("fwlb"), resultMap("lsqz"), resultMap("zcggrq"))
    Some(ret)
  }
}

object SearchBrandHelpers extends SearchBrandHelpers

trait SearchBrandHelpers {
  def searchBrandByRegNo(regno: String): Option[SearchBrandResult] = {
    if (regno == null) {
      return None
    }

    val ret = SearchBrandActor !? SearchBrandRegno(regno)
    ret.asInstanceOf[Option[SearchBrandResult]]
  }
}