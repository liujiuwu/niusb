package code.lib

import scala.xml.NodeSeq
import code.model.BrandType
import scala.collection.mutable.ArrayBuffer

object SelectBoxHelper {
  lazy val orderTypes = List[(String, String)]("0" -> "由新至旧", "1" -> "价格从低至高", "2" -> "价格从高至低", "3" -> "推荐", "4" -> "热门")
  lazy val likeTypes = List[(String, String)]("0" -> "精确", "1" -> "模糊", "2" -> "前包含", "3" -> "后包含")
  lazy val keywordTypes = List[(String, String)]("0" -> "商标名称", "1" -> "商标注册号")
  lazy val brandTypes = BrandType.getBrandTypes().values.toList

  def orderOptions(selected: String): NodeSeq = {
    for (option <- orderTypes; (value, label) = option) yield {
      options(value, label, selected)
    }
  }

  def likeOptions(selected: String): NodeSeq = {
    for (option <- likeTypes; (value, label) = option) yield {
      options(value, label, selected)
    }
  }
  
  def keywordTypeOptions(selected: String): NodeSeq = {
    for (option <- keywordTypes; (value, label) = option) yield {
      options(value, label, selected)
    }
  }

  def brandTypeOptions(selected: String): NodeSeq = {
    for (option <- brandTypes; (value, label) = (option.code.get.toString, option.name.get)) yield {
      options(value, label, selected, true)
    }
  }

  private def options(value: String, label: String, selected: String, prependValue: Boolean = false) = {
    <option value={ value } selected={ if (selected == value) "selected" else null }>{ if (prependValue) value + "." + label else label }</option>
  }

}