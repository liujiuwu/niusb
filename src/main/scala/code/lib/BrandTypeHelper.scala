package code.lib

import scala.collection.mutable.LinkedHashMap
import code.model.WendaType2

case class BrandType2(id: Int, name: String, descn: String)

object BrandType2Helper extends App {
  val BrandType2s = LinkedHashMap[Int, BrandType2](
    1 -> BrandType2(1, "化学原料", ""),
    2 -> BrandType2(2, "颜料油漆", ""),
    3 -> BrandType2(3, "化妆品", ""),
    4 -> BrandType2(4, "燃料油脂", ""),
    5 -> BrandType2(5, "医药卫生", ""),
    6 -> BrandType2(6, "五金金属", ""),
    7 -> BrandType2(7, "机械设备", ""),
    8 -> BrandType2(8, "手工器械", ""),
    9 -> BrandType2(9, "电子仪器", ""),
    10 -> BrandType2(10, "医疗器械", ""),
    11 -> BrandType2(11, "家电", ""),
    12 -> BrandType2(12, "运输工具", ""),
    13 -> BrandType2(13, "军火烟花", ""),
    14 -> BrandType2(14, "珠宝钟表", ""),
    15 -> BrandType2(15, "乐器", ""),
    16 -> BrandType2(16, "文化用品", ""),
    17 -> BrandType2(17, "橡胶制品", ""),
    18 -> BrandType2(18, "皮革皮具", ""),
    19 -> BrandType2(19, "建筑材料", ""),
    20 -> BrandType2(20, "家具", ""),
    21 -> BrandType2(21, "日用品", ""),
    22 -> BrandType2(22, "绳网袋篷", ""),
    23 -> BrandType2(23, "纺织纱线", ""),
    24 -> BrandType2(24, "床上用品", ""),
    25 -> BrandType2(25, "服装鞋帽", ""),
    26 -> BrandType2(26, "花边拉链", ""),
    27 -> BrandType2(27, "地毯席垫", ""),
    28 -> BrandType2(28, "体育玩具", ""),
    29 -> BrandType2(29, "干货油奶", ""),
    30 -> BrandType2(30, "食品调味", ""),
    31 -> BrandType2(31, "水果花木", ""),
    32 -> BrandType2(32, "啤酒饮料", ""),
    33 -> BrandType2(33, "酒", ""),
    34 -> BrandType2(34, "烟草烟具", ""),
    35 -> BrandType2(35, "广告贸易", ""),
    36 -> BrandType2(36, "金融物管", ""),
    37 -> BrandType2(37, "建筑修理", ""),
    38 -> BrandType2(38, "通讯电信", ""),
    39 -> BrandType2(39, "运输旅行", ""),
    40 -> BrandType2(40, "材料加工", ""),
    41 -> BrandType2(41, "教育娱乐", ""),
    42 -> BrandType2(42, "科研服务", ""),
    43 -> BrandType2(43, "餐饮酒店", ""),
    44 -> BrandType2(44, "医疗园艺", ""),
    45 -> BrandType2(45, "社会法律", ""))

  val es = BrandType2s.values.toList
  es.zipWithIndex.foreach {
    case (v, i) => {
      println(s"""INSERT INTO `brand_types` VALUES ('${i+1}', '${v.id}', '${v.name}', '0', '0', '${v.name}');""")

    }
  }

 /* val es = WendaType2.values.toList
		  es.zipWithIndex.foreach {
		  case (v, i) => {
			  println(s"""INSERT INTO `wenda_types` VALUES ('${i+1}', '${v.id}', '${v}', '0', '0', '${v}');""")
			  
		  }
  }*/
}


