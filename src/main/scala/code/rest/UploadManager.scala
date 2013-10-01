package code.rest

import java.io.File
import java.util.Date
import org.apache.commons.io.FileUtils
import com.sksamuel.scrimage.Image
import net.liftweb.common.Box
import net.liftweb.common.Box.option2Box
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.FileParamHolder
import net.liftweb.http.InMemoryResponse
import net.liftweb.http.LiftRules
import net.liftweb.http.LiftRulesMocker.toLiftRules
import net.liftweb.http.provider.servlet.HTTPServletContext
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonDSL.jobject2assoc
import net.liftweb.json.JsonDSL.long2jvalue
import net.liftweb.json.JsonDSL.pair2Assoc
import net.liftweb.json.JsonDSL.pair2jvalue
import net.liftweb.json.JsonDSL.string2jvalue
import net.liftweb.util.StringHelpers
import com.sksamuel.scrimage.Format
import net.liftweb.http.JsonResponse
import com.niusb.util.UploadHelpers
import net.liftweb.json.JsonAST.JObject

object UploadManager extends RestHelper with Loggable {
  serve {
    case "uploading" :: Nil Post req => {
      def saveImage(fph: FileParamHolder): JObject = {
        if (fph.mimeType != "image/jpeg" && fph.mimeType != "image/png") {
          return ("error" -> "只能上传jpeg/png格式的图片文件，请确认！")
        }

        val newFileName = UploadHelpers.genNewFileName()
        val uploadFileName = UploadHelpers.uploadTmpDir + File.separator + newFileName

        val oImg = Image(fph.fileStream)
        UploadHelpers.myFit(oImg, (400, 300), (oImg.width, oImg.height)).writer(Format.JPEG).withProgressive(true).withCompression(100).write(uploadFileName)
        ("name" -> newFileName) ~ ("type" -> fph.mimeType) ~ ("size" -> fph.length)
      }

      val ojv: Box[JValue] = req.uploadedFiles.map(fph => saveImage(fph)).headOption
      val ajv = ("name" -> "n/a") ~ ("type" -> "n/a") ~ ("size" -> 0L)
      println(ojv + "**********************")
      val ret = ojv openOr ajv

      val jr = JsonResponse(ret).toResponse.asInstanceOf[InMemoryResponse]
      InMemoryResponse(jr.data, ("Content-Length", jr.data.length.toString) ::
        ("Content-Type", "text/plain") :: Nil, Nil, 200)
    }
  }
}
