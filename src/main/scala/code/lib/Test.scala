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

  val mobile = """注册号/申请号 9999874  国际分类号 25  申请日期 2011-09-23  申请人名称(中文) 卡哇依集团有限公司  申请人地址(中文) 香港中环德辅道中121号远东发展大厦21楼03室   KAWAYI GROUP CO.,LIMITED   FLAT/RM 2103 FAR EAST CONSORTIUM BLDG 121 DES VOEUX RD CENTRAL HK   商品/服务列表 服装;童装;游泳衣;鞋;帽;袜;手套(服装);围巾;皮带(服饰用);雨衣;  类似群 2501 2503 2504 2507 2508 2509 2510 2511 2512   初审公告期号 1330 注册公告期号 1342  初审公告日期 2012-10-06  注册公告日期 2013-01-07  专用权期限 2013年01月07日 至 2023年01月06日  年 后期指定日期  国际注册日期  优先权日期 无 代理人名称 北京弘智信知识产权代理有限公司  指定颜色  商标类型 普通商标 是否共有商标 否   备注   商标流程   前一页   后一页   仅供参考，无任何法律效力，请核实后使用""".replaceAll("\n", "")
  val mobileRegx = """类似群(.+)初审公告期号""".r
  mobileRegx.findFirstMatchIn(mobile).groupBy(g => println(g.group(1)))

}