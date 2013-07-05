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

object UploadManager extends RestHelper with Loggable {
  def getBaseApplicationPath: Box[String] =
    {
      LiftRules.context match {
        case context: HTTPServletContext =>
          {
            var baseApp: String = context.ctx.getRealPath("/")

            if (!baseApp.endsWith(File.separator))
              baseApp = baseApp + File.separator

            Full(baseApp)
          }
        case _ => Empty
      }
    }

  serve {
    case "uploading" :: Nil Post req => {
      def saveImage(fph: FileParamHolder) =  {
        val newFileName = StringHelpers.randomString(16)+".jpg"
        val uploadDir = getBaseApplicationPath.get + File.separator + "upload" + File.separator + newFileName
        Thumbnails.of(fph.fileStream)
          .size(400, 300)
          .outputQuality(1f)
          .toFile(new File(uploadDir));

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
