package code.rest

import java.io.File
import net.liftweb.common.Box
import net.liftweb.common.Box.option2Box
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.FileParamHolder
import net.liftweb.http.InMemoryResponse
import net.liftweb.http.JsonResponse
import net.liftweb.http.LiftRules
import net.liftweb.http.LiftRulesMocker.toLiftRules
import net.liftweb.http.OnDiskFileParamHolder
import net.liftweb.http.provider.servlet.HTTPServletContext
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonDSL.jobject2assoc
import net.liftweb.json.JsonDSL.long2jvalue
import net.liftweb.json.JsonDSL.pair2Assoc
import net.liftweb.json.JsonDSL.pair2jvalue
import net.liftweb.json.JsonDSL.string2jvalue
import net.coobird.thumbnailator.Thumbnails
import net.liftweb.util.StringHelpers
import com.sksamuel.scrimage.Image
import java.awt.Graphics2D

object UploadManager extends RestHelper with Loggable {
  def myFit(img: Image, t: (Int, Int), source: (Int, Int)): Image = {
    val targetWidth = t._1
    val targetHeight = t._2
    val fittedDimensions = dimensionsToFit((targetWidth, targetHeight), (source._1, source._2))
    val scaled = img.scaleTo(fittedDimensions._1, fittedDimensions._2)
    val target = Image.filled(targetWidth, targetHeight, java.awt.Color.WHITE)
    val g2 = target.awt.getGraphics.asInstanceOf[Graphics2D]
    val x = ((targetWidth - fittedDimensions._1) / 2.0).toInt
    val y = ((targetHeight - fittedDimensions._2) / 2.0).toInt
    g2.drawImage(scaled.awt, x, y, null)
    g2.dispose()
    target
  }

  def dimensionsToFit(target: (Int, Int), source: (Int, Int)): (Int, Int) = {
    val maxWidth = if (target._1 == 0) source._1 else target._1
    val maxHeight = if (target._2 == 0) source._2 else target._2

    val wscale = maxWidth / source._1.toDouble
    val hscale = maxHeight / source._2.toDouble

    if (source._1 < target._1 && source._2 < target._2) {
      return source
    }

    if (wscale < hscale)
      ((source._1 * wscale).toInt, (source._2 * wscale).toInt)
    else
      ((source._1 * hscale).toInt, (source._2 * hscale).toInt)
  }

  def getBaseApplicationPath: Box[String] = {
    LiftRules.context match {
      case context: HTTPServletContext =>
        {
          var baseApp: String = context.ctx.getRealPath("/")
          if (!baseApp.endsWith(File.separator)) {
            baseApp = baseApp + File.separator
          }
          Full(baseApp)
        }
      case _ => Empty
    }
  }

  def getUploadDirTmp: File = {
    val dir = new File(getUploadDir + File.separator + "tmp")
    if (!dir.exists()) {
      dir.mkdirs()
    }
    dir
  }

  def getUploadDir: File = {
    val dir = new File(getBaseApplicationPath.get + File.separator + "upload")
    if (!dir.exists()) {
      dir.mkdirs()
    }
    dir
  }

  serve {
    case "uploading" :: Nil Post req => {
      def saveImage(fph: FileParamHolder) = {
        val newFileName = StringHelpers.randomString(16) + ".png"
        val uploadFileName = getUploadDirTmp + File.separator + newFileName

        val oImg = Image(fph.fileStream)
        val swh = (oImg.width, oImg.height)
        myFit(oImg, (400, 300), swh).write(uploadFileName)
        //oImg.fit(wh._1, wh._2).write(uploadFileName)

        /*Thumbnails.of(fph.fileStream)
          .size(wh._1,wh._2)
          .outputQuality(1f)
          .toFile(new File(uploadFileName));*/

        ("name" -> newFileName) ~ ("type" -> fph.mimeType) ~ ("size" -> fph.length)
      }

      val ojv: Box[JValue] = req.uploadedFiles.map(fph => saveImage(fph)).headOption
      val ajv = ("name" -> "n/a") ~ ("type" -> "n/a") ~ ("size" -> 0L)
      val ret = ojv openOr ajv

      val jr = JsonResponse(ret).toResponse.asInstanceOf[InMemoryResponse]
      InMemoryResponse(jr.data, ("Content-Length", jr.data.length.toString) ::
        ("Content-Type", "text/plain") :: Nil, Nil, 200)
    }
  }
}
