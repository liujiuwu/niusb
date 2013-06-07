package code.lib

import scala.collection.mutable.LinkedHashMap


case class BrandType(id: Int, name: String, descn: String)

object BrandTypeHelper {
  val brandTypes = LinkedHashMap[Int, BrandType](
    1 -> BrandType(1, "化学原料", ""),
    2 -> BrandType(2, "颜料油漆", ""),
    3 -> BrandType(3, "化妆品", ""),
    4 -> BrandType(4, "燃料油脂", ""),
    5 -> BrandType(5, "医药卫生", ""),
    6 -> BrandType(6, "五金金属", ""),
    7 -> BrandType(7, "机械设备", ""),
    8 -> BrandType(8, "手工器械", ""),
    9 -> BrandType(9, "电子仪器", ""),
    10 -> BrandType(10, "医疗器械", ""),
    11 -> BrandType(11, "家电", ""),
    12 -> BrandType(12, "运输工具", ""),
    13 -> BrandType(13, "军火烟花", ""),
    14 -> BrandType(14, "珠宝钟表", ""),
    15 -> BrandType(15, "乐器", ""),
    16 -> BrandType(16, "文化用品", ""),
    17 -> BrandType(17, "橡胶制品", ""),
    18 -> BrandType(18, "皮革皮具", ""),
    19 -> BrandType(19, "建筑材料", ""),
    20 -> BrandType(20, "家具", ""),
    21 -> BrandType(21, "日用品", ""),
    22 -> BrandType(22, "绳网袋篷", ""),
    23 -> BrandType(23, "纺织纱线", ""),
    24 -> BrandType(24, "床上用品", ""),
    25 -> BrandType(25, "服装鞋帽", ""),
    26 -> BrandType(26, "花边拉链", ""),
    27 -> BrandType(27, "地毯席垫", ""),
    28 -> BrandType(28, "体育玩具", ""),
    29 -> BrandType(29, "干货油奶", ""),
    30 -> BrandType(30, "食品调味", ""),
    31 -> BrandType(31, "水果花木", ""),
    32 -> BrandType(32, "啤酒饮料", ""),
    33 -> BrandType(33, "酒", ""),
    34 -> BrandType(34, "烟草烟具", ""),
    35 -> BrandType(35, "广告贸易", ""),
    36 -> BrandType(36, "金融物管", ""),
    37 -> BrandType(37, "建筑修理", ""),
    38 -> BrandType(38, "通讯电信", ""),
    39 -> BrandType(39, "运输旅行", ""),
    40 -> BrandType(40, "材料加工", ""),
    41 -> BrandType(41, "教育娱乐", ""),
    42 -> BrandType(42, "科研服务", ""),
    43 -> BrandType(43, "餐饮酒店", ""),
    44 -> BrandType(44, "医疗园艺", ""),
    45 -> BrandType(45, "社会法律", ""))

}