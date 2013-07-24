package code.snippet

import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.http.FileParamHolder
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml._
import java.io.FileOutputStream
import java.io.File
import net.liftweb.http.provider.servlet.HTTPServletContext
import net.liftweb.http.LiftRules

class FileUploadSnippet extends Loggable {
  def render = {

    var upload: Box[FileParamHolder] = Empty

    def processForm() = upload match {
      case Full(FileParamHolder(_, mimeType, fileName, file)) =>
        logger.info("%s of type %s is %d bytes long %s" format (fileName, mimeType, file.length, hexEncode(md5(file))))
        logger.info(getBaseApplicationPath)
        var output = new FileOutputStream(new File("d:\\tmp\\" + fileName))
        output.write(file)
        output.close()
      case _ => logger.warn("No file?")
    }

    "#file" #> fileUpload(f => upload = Full(f)) &
      "type=submit" #> onSubmitUnit(processForm)
  }

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
}