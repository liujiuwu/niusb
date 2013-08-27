package code.lib

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import code.model.MyDBVendor
import code.model.User
import net.liftweb.db.DB
import net.liftweb.db.DefaultConnectionIdentifier
import net.liftweb.mapper.Schemifier
import code.model.Brand
import java.text.SimpleDateFormat
import net.liftweb.util.Helpers

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

  /*val mobile = """注册号/申请号 9999874  国际分类号 25  申请日期 2011-09-23  申请人名称(中文) 卡哇依集团有限公司  申请人地址(中文) 香港中环德辅道中121号远东发展大厦21楼03室   KAWAYI GROUP CO.,LIMITED   FLAT/RM 2103 FAR EAST CONSORTIUM BLDG 121 DES VOEUX RD CENTRAL HK   商品/服务列表 服装;童装;游泳衣;鞋;帽;袜;手套(服装);围巾;皮带(服饰用);雨衣;  类似群 2501 2503 2504 2507 2508 2509 2510 2511 2512   初审公告期号 1330 注册公告期号 1342  初审公告日期 2012-10-06  注册公告日期 2013-01-07  专用权期限 2013年01月07日 至 2023年01月06日  年 后期指定日期  国际注册日期  优先权日期 无 代理人名称 北京弘智信知识产权代理有限公司  指定颜色  商标类型 普通商标 是否共有商标 否   备注   商标流程   前一页   后一页   仅供参考，无任何法律效力，请核实后使用""".replaceAll("\n", "")
  val mobileRegx = """类似群(.+)初审公告期号""".r
  mobileRegx.findFirstMatchIn(mobile).groupBy(g => println(g.group(1)))*/

  /*val regno = "10697213"
  val home = "http://sbcx.saic.gov.cn/trade"
  val regnoUrl = s"""http://sbcx.saic.gov.cn/trade/servlet?Search=FL_REG_List&RegNO=${regno}"""

  val httpclient = new DefaultHttpClient()
  val get = new HttpGet(regnoUrl)
  get.addHeader("Referer", home)
  get.setHeader("Host", "sbcx.saic.gov.cn");
  get.setHeader("User-Agent", "User-Agent	Mozilla/5.0 (Windows NT 6.1; WOW64; rv:21.0) Gecko/20100101 Firefox/21.0");
  
  
  val response = httpclient.execute(get)
  
  println(EntityUtils.toString(response.getEntity()))*/

  /*val t1 = "1EBPJZTEAIDIXEXW.jpg"
  val t2 = "31EBPJZTEAIDIXEXW.jpg"
  val r = """([\w]+).(jpg|jpeg|png)""".r
  t1 match {
    case r(f, e) => println(f + "x320|" + e)
    case _ => println("no")
  }*/
  
  
  /*lazy val sdf = new SimpleDateFormat("yyyy-M-dd")
  lazy val sdf2 = new SimpleDateFormat("yyyy-MM-dd")
  println(sdf2.format(sdf.parse("2002-2-14")))*/
  println(Helpers.randomInt(6))

}