package com.niusb.util

import net.liftweb.http.provider.servlet.HTTPServletContext
import java.awt.Graphics2D
import java.text.SimpleDateFormat
import net.liftweb.http.LiftRules
import net.liftweb.common.Full
import net.liftweb.common.Box
import org.apache.commons.io.FileUtils
import java.io.File
import net.liftweb.common.Empty
import com.sksamuel.scrimage.Image
import java.util.Date
import net.liftweb.util.StringHelpers
import net.liftweb.common.Box.box2Option
import net.liftweb.http.LiftRulesMocker.toLiftRules

object UploadHelpers extends UploadHelpers

trait UploadHelpers {
  val fmt = new SimpleDateFormat("yyyyMMddHHmmss")
  val hmsFmt = new SimpleDateFormat("HHmmss")
  val ymdFmt = new SimpleDateFormat("yyyyMMdd")

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
    scaled
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

  def appPath(): Box[String] = {
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

  def uploadTmpDir: File = uploadFileDir()
  def uploadBrandDir(rootPath: Box[String] = Empty): File = {
    val path = "brand" + File.separator + ymdFmt.format(new Date)
    uploadModuleDir(path, rootPath)
  }

  private def uploadModuleDir(path: String, rootPath: Box[String] = Empty): File = {
    rootPath match {
      case Full(r) =>
        uploadFileDir(path, rootPath)
      case _ => uploadFileDir(path)
    }
  }

  private def uploadFileDir(sub: String = "tmp", rootPath: Box[String] = appPath()): File = {
    val dir = new File(uploadDir(rootPath) + File.separator + sub)
    if (!dir.exists()) {
      dir.mkdirs()
    }
    dir
  }

  private def uploadDir(rootPath: Box[String]): File = {
    val dir = new File(rootPath.get + File.separator + "upload")
    if (!dir.exists()) {
      dir.mkdirs()
    }
    dir
  }

  def srcPath(fileName: String, module: String = "brand") = {
    if (fileName != null && !fileName.trim.isEmpty()) {
      val dir = fileName.substring(0, 8)
      s"/upload/${module}/${dir}/${fileName}"
    } else {
      ""
    }
  }

  def srcTmpPath(fileName: String) = s"/upload/tmp/${fileName}"

  def genNewFileName(extension: String = "jpg") = fmt.format(new Date) + "_" + StringHelpers.randomString(16) + "." + extension

  def handleBrandImg(pic: String) = {
    if (pic != null && !pic.trim().isEmpty()) {
      val srcPic = new File(uploadTmpDir + File.separator + pic)
      val destPic = new File(uploadBrandDir() + File.separator + pic)
      if (srcPic.exists()) {
        FileUtils.moveFile(srcPic, destPic)
      }
    }
  }
}