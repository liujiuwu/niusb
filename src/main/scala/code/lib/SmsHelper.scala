package code.lib

import scala.collection.mutable.LinkedHashSet
import scala.util.Random
import net.liftweb.actor.LiftActor
import net.liftweb.common.Loggable
import net.liftweb.util.Helpers._

case class SendSms(mobile: String, sms: String, sign: String = "牛标网")
case class SmsCode(code: String, cacheTime: Int = WebHelper.now)

object SmsActor extends LiftActor {
  def messageHandler = {
    case SendSms(mobile, sms, sign) => sendSms(mobile, sms, sign)
    case _ =>
  }

  private def sendSms(mobile: String, sms: String, sign: String) {
    println(mobile + "|" + sms + "|【" + sign + "】")
  }

}

object SmsHelper extends App with Loggable {
  def random(length: Int = 6) = {
    val result = LinkedHashSet[Int]()
    while (result.size < length) {
      result.add(Random.nextInt(10))
    }
    result.mkString
  }

  def sendCodeSms(mobile: String) {
    val code = random()
    val sms = s"您正在登录牛标网，校验码：${code}。泄露有风险，请在5分钟内使用此验证码。"
    MemcachedHelper.set(mobile, SmsCode(code), 5 minutes)
    logger.info(mobile + "|" + code)
    sendSms(mobile, sms)
  }

  def sendSms(mobile: String, sms: String) {
    SmsActor ! SendSms(mobile, sms)
  }

  def getSendSmsCode(mobile: String): Option[SmsCode] = MemcachedHelper.get(mobile) match {
    case Some(sc) => Option(sc.asInstanceOf[SmsCode])
    case _ => None
  }

  def smsCode(mobile: String): (String, Int) = {
    getSendSmsCode(mobile) match {
      case Some(sc) => (sc.code, sc.cacheTime)
      case _ => ("", 0)
    }
  }

  //sendCodeSms("123456")
  //println(getSendSmsCode("123456"))
}