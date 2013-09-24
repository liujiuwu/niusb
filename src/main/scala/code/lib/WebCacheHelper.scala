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
import code.model.WendaType
import code.model.BrandStatus
import code.model.Webset

object WebCacheHelper extends Loggable {
  val websets = LinkedHashMap[Long, Webset]()
  def loadWebsets() {
    logger.info("load webset ...")
    websets.clear()
    Webset.findAll(MaxRows(1)).headOption match {
      case Some(webset) => websets.put(webset.id.is, webset)
      case _ =>
    }
    logger.info("load webset finished.")
  }

  val brandTypes = LinkedHashMap[Int, BrandType]()
  def loadBrandTypes() {
    logger.info("load brandTypes ...")
    brandTypes.clear()
    BrandType.findAll(OrderBy(BrandType.code, Ascending)).foreach(brandType => brandTypes.put(brandType.code.is, brandType))
    logger.info("load brandTypes finished.")
  }

  val wendaTypes = LinkedHashMap[Int, WendaType]()
  def loadWendaTypes() {
    logger.info("load wendaTypes ...")
    wendaTypes.clear()
    WendaType.findAll(OrderBy(WendaType.code, Ascending)).foreach(wendaType => wendaTypes.put(wendaType.code.is, wendaType))
    logger.info("load wendaTypes finished.")
  }

  val adSpaces = LinkedHashMap[Int, AdSpace]()
  def loadAdSpaces() {
    logger.info("load adSpaces ...")
    adSpaces.clear()
    val now = new Date
    AdSpace.findAll(By_<(AdSpace.startTime, now), By_>=(AdSpace.endTime, now)).foreach(adSpace => {
      adSpaces.put(adSpace.code.get, adSpace)
      adSpace.ads = Ad.findAll(By(Ad.adSpaceCode, adSpace.code.get))
    })
  }

  val indexTabBrands = LinkedHashMap[String, List[Brand]]() //首页tab数据
  def loadIndexTabBrands(force: Boolean = false, limit: Int = 21) {
    logger.info("load indexTabBrands ...")
    indexTabBrands.clear()
    for (i <- 0 to 3) {
      i match {
        case 0 =>
          val brands = Brand.findAll(By(Brand.status, BrandStatus.ChuShoZhong), By(Brand.isRecommend, true), StartAt(0), MaxRows[Brand](limit), OrderBy(Brand.id, Descending))
          indexTabBrands.put("0", brands)
        case 1 =>
          val brands = Brand.findAll(By(Brand.status, BrandStatus.ChuShoZhong), StartAt(0), MaxRows[Brand](limit), OrderBy(Brand.id, Descending))
          indexTabBrands.put("1", brands)
        case 2 =>
          val brands = Brand.findAll(By(Brand.status, BrandStatus.ChuShoZhong), StartAt(0), MaxRows[Brand](limit), OrderBy(Brand.viewCount, Descending))
          indexTabBrands.put("2", brands)
        case 3 =>
          val brands = Brand.findAll(By(Brand.status, BrandStatus.ChuShoZhong), By(Brand.isOwn, true), StartAt(0), MaxRows[Brand](limit), OrderBy(Brand.id, Descending))
          indexTabBrands.put("3", brands)
      }
    }
    logger.info("load indexTabBrands finished.")
  }

  val indexBrandsByType = LinkedHashMap[Int, List[Brand]]() //首页分类数据
  def loadIndexBrandTypeBrands(force: Boolean = false, limit: Int = 35) {
    logger.info("load indexTabBrands ...")
    indexBrandsByType.clear()
    val brandTypeCodes = List(25, 3, 43)
    for (brandTypeCode <- brandTypeCodes) {
      val brands = Brand.findAll(By(Brand.status, BrandStatus.ChuShoZhong), By(Brand.brandTypeCode, brandTypeCode), StartAt(0), MaxRows[Brand](limit), OrderBy(Brand.id, Descending))
      indexBrandsByType.put(brandTypeCode, brands)
    }
    logger.info("load indexTabBrands finished.")
  }

  def load(force: Boolean = false) {
    logger.info("load cahche start ...")
    val startTime = System.currentTimeMillis()
    loadWebsets()
    loadBrandTypes()
    loadWendaTypes()
    loadAdSpaces()
    loadIndexTabBrands()
    loadIndexBrandTypeBrands()
    logger.info("load cahche finished use time " + (System.currentTimeMillis() - startTime) + " ms")
  }

}