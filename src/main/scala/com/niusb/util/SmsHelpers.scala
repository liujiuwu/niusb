package com.niusb.util

import java.util.Date
import scala.collection.mutable.LinkedHashSet
import scala.util.Random
import net.liftweb.actor.LiftActor
import net.liftweb.util.Helpers.intToTimeSpanBuilder
import java.net.HttpURLConnection
import java.io.BufferedReader
import java.net.URLEncoder
import java.net.URL
import java.io.InputStreamReader
import net.liftweb.common.Loggable

case class SendSms(mobile: String, sms: String, sign: String = "牛标网")
case class SmsCode(code: String, cacheTime: Int = WebHelpers.now)

object SmsActor extends LiftActor with Loggable {
  lazy val smsUrl = """http://www.gysoft.cn/smspost_utf8/send.aspx?"""
  def messageHandler = {
    case SendSms(mobile, sms, sign) => sendSms(mobile, sms, sign)
    case _ =>
  }

  def sendSms(mobile: String, sms: String, sign: String) {
    val smsBuf = new StringBuffer(smsUrl)
    smsBuf.append("username=niubiao&password=821024")
    smsBuf.append(s"&mobile=${mobile}")
    smsBuf.append("&content=" + URLEncoder.encode(sms, "UTF-8"))
    val url = new URL(smsBuf.toString())
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    val in = new BufferedReader(new InputStreamReader(url.openStream()))
    val result = in.readLine()
    logger.info(s"sendSms|${mobile}|${result}")
  }
}

object SmsHelpers extends SmsHelpers

trait SmsHelpers {
  def random(length: Int = 6) = {
    val result = LinkedHashSet[Int]()
    while (result.size < length) {
      result.add(Random.nextInt(10))
    }
    result.mkString
  }

  private def getCountKey(mobile: String) = {
    mobile + "_count_" + WebHelpers.ndf.format(new Date)
  }

  def getSendSmsCount(mobile: String): Int = {
    val sendCount = MemHelpers.get(getCountKey(mobile)) match {
      case Some(count) => count.asInstanceOf[Int]
      case _ => 0
    }
    sendCount
  }

  def sendCodeSms(action: String, mobile: String) {
    val code = random()
    val sms = s"您正在${action}，校验码：${code}。泄露有风险，请在5分钟内使用此验证码。"
    MemHelpers.set(mobile, SmsCode(code), 5 minute)
    MemHelpers.set(getCountKey(mobile), getSendSmsCount(mobile) + 1, 1 day)
    sendSms(mobile, sms)
  }

  def sendSms(mobile: String, sms: String) {
    SmsActor ! SendSms(mobile, sms)
  }

  def getSendSmsCode(mobile: String): Option[SmsCode] = MemHelpers.get(mobile) match {
    case Some(sc) => Option(sc.asInstanceOf[SmsCode])
    case _ => None
  }

  def smsCode(mobile: String): SmsCode = {
    getSendSmsCode(mobile) match {
      case Some(sc) => SmsCode(sc.code, sc.cacheTime)
      case _ => SmsCode("", 0)
    }
  }
}