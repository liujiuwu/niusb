package code.lib

import java.util.Date
import scala.collection.mutable.LinkedHashMap
import scala.collection.mutable.LinkedList
import code.model.Ad
import code.model.AdSpace
import code.model.Brand
import code.model.BrandType
import net.liftweb.mapper._
import net.liftweb.common.Loggable

object WebCacheHelper extends Loggable {
  val brandTypes = LinkedHashMap[Int, BrandType]()
  def loadBrandTypes(force: Boolean = false) {
    if (brandTypes.isEmpty || force) {
      logger.info("load brandTypes ...")
      brandTypes.clear()
      BrandType.findAll(OrderBy(BrandType.code, Ascending)).foreach(brandType => brandTypes.put(brandType.code.get, brandType))
      logger.info("load brandTypes finished.")
    }
  }

  val adSpaces = LinkedHashMap[Int, AdSpace]()
  def loadAdSpaces(force: Boolean = false) {
    if (adSpaces.isEmpty || force) {
      logger.info("load adSpaces ...")
      adSpaces.clear()
      val now = new Date
      AdSpace.findAll(By_<(AdSpace.startTime, now), By_>=(AdSpace.endTime, now)).foreach(adSpace => {
        adSpaces.put(adSpace.code.get, adSpace)
        adSpace.ads = Ad.findAll(By(Ad.adSpaceCode, adSpace.code.get))
      })
      logger.info("load adSpaces finished.")
    }
  }

  val indexTabBrands = LinkedHashMap[String, List[Brand]]() //首页tab数据
  def loadIndexTabBrands(force: Boolean = false, limit: Int = 18) {
    if (indexTabBrands.isEmpty || force) {
      logger.info("load indexTabBrands ...")
      indexTabBrands.clear()
      for (i <- 0 to 3) {
        i match {
          case 0 =>
            val brands = Brand.findAll(MaxRows[Brand](limit), OrderBy(Brand.id, Descending))
            indexTabBrands.put("0", brands)
          case 1 =>
            val brands = Brand.findAll(StartAt(30), MaxRows[Brand](limit), OrderBy(Brand.id, Descending))
            indexTabBrands.put("1", brands)
          case 2 =>
            val brands = Brand.findAll(StartAt(0), MaxRows[Brand](limit), OrderBy(Brand.createdAt, Descending))
            indexTabBrands.put("2", brands)
          case 3 =>
            val brands = Brand.findAll(StartAt(0), MaxRows[Brand](limit), OrderBy(Brand.name, Descending))
            indexTabBrands.put("3", brands)
        }
      }
      logger.info("load indexTabBrands finished.")
    }
  }

  val indexBrandsByType = LinkedHashMap[Int, List[Brand]]() //首页分类数据
  def loadIndexBrandTypeBrands(force: Boolean = false, limit: Int = 30) {
    if (indexBrandsByType.isEmpty || force) {
      logger.info("load indexTabBrands ...")
      indexBrandsByType.clear()
      val brandTypeCodes = List(25, 3, 43)
      for (brandTypeCode <- brandTypeCodes) {
        val brands = Brand.findAll(By(Brand.brandTypeCode, brandTypeCode), StartAt(0), MaxRows[Brand](limit), OrderBy(Brand.createdAt, Descending))
        indexBrandsByType.put(brandTypeCode, brands)
      }
      logger.info("load indexTabBrands finished.")
    }
  }

  def load(force: Boolean = false) {
    logger.info("load cahche start ...")
    val startTime = System.currentTimeMillis()
    loadBrandTypes(force)
    loadAdSpaces(force)
    loadIndexTabBrands(force)
    loadIndexBrandTypeBrands(force)
    logger.info("load cahche finished use time " + (System.currentTimeMillis() - startTime) + " ms")
  }

}