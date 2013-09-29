package com.niusb.util

import scala.collection.mutable.LinkedHashSet
import scala.util.Random
import net.liftweb.actor.LiftActor
import net.liftweb.common.Loggable
import net.liftweb.util.Helpers._
import java.util.Date

case class SendSms(mobile: String, sms: String, sign: String = "牛标")
case class SmsCode(code: String, cacheTime: Int = WebHelpers.now)

object SmsActor extends LiftActor {
  def messageHandler = {
    case SendSms(mobile, sms, sign) => sendSms(mobile, sms, sign)
    case _ =>
  }

  private def sendSms(mobile: String, sms: String, sign: String) {
    println(mobile + "|" + sms + "|【" + sign + "】")
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
    mobile + "_count_" + WebHelpers.df.format(new Date)
  }

  def getSendSmsCount(mobile: String): Int = {
    val sendCount = MemHelpers.get(getCountKey(mobile)) match {
      case Some(count) => 0//count.asInstanceOf[Int]
      case _ => 0
    }
    sendCount
  }

  def sendCodeSms(action: String, mobile: String) {
    val code = random()
    val sms = s"您正在${action}，校验码：${code}。泄露有风险，请在5分钟内使用此验证码。"
    MemHelpers.set(mobile, SmsCode(code), 5 minutes)
    MemHelpers.inc(getCountKey(mobile))
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