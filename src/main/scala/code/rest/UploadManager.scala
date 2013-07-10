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
import javax.imageio.ImageIO
import com.sksamuel.scrimage.Image

object UploadManager extends RestHelper with Loggable {
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

  def scaleWh(width: Int, height: Int, maxWidth: Int = 400, maxHeight: Int = 300): (Int, Int) = {
    if (width <= maxWidth && height <= maxHeight) {
      return (width, height)
    }
    if (width > height) (maxWidth, height * maxWidth / width) else (width * maxHeight / height, maxHeight)
  }

  serve {
    case "uploading" :: Nil Post req => {
      def saveImage(fph: FileParamHolder) = {
        val newFileName = StringHelpers.randomString(16) + ".png"
        val uploadFileName = getUploadDirTmp + File.separator + newFileName

        val originalImg = Image(fph.fileStream)
        val width = originalImg.width
        val height = originalImg.height
        val wh = scaleWh(width, height)

        originalImg.fit(wh._1,wh._2).write(uploadFileName)
        
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
