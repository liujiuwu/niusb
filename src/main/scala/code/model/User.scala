package code.model

import net.liftweb.mapper._
import net.liftweb.common.Full
import java.text.SimpleDateFormat
import code.lib.WebHelper
import net.liftweb.common.Box
import code.lib.SmsHelper
import code.lib.MemcachedHelper

object UserType extends Enumeration {
  type UserType = Value
  val Normal = Value(0, "普通用户")
  val Vip = Value(1, "VIP用户")
  val Agent = Value(2, "代理用户")
}

object UserStatus extends Enumeration {
  type UserStatus = Value
  val Normal = Value(0, "正常")
  val Disabled = Value(1, "禁止")
}

object UserSupper extends Enumeration {
  type UserSupper = Value
  val Normal = Value(0, "否")
  val Supper = Value(1, "是")
}

class User extends MegaProtoUser[User] with LongKeyedMapper[User] with CreatedUpdated {
  def getSingleton = User

  object name extends MappedString(this, 20) {
    override def validations = valMinLen(2, "真实姓名或昵称，不少于2个字。") _ :: super.validations
  }

  object gender extends MappedGender(this)

  object mobile extends MappedString(this, 15) {
    override def dbIndexed_? = true
    override def dbColumnName = "mobile"
    override def validations = valUnique("手机号已经存在，请确认") _ :: valMinLen(11, "手机号码不少于11位数字。") _ :: super.validations
  }

  object phone extends MappedString(this, 90) {
    override def dbColumnName = "phone"
  }

  object qq extends MappedString(this, 20) {
    override def dbColumnName = "qq"
  }

  object userType extends MappedEnum(this, UserType) {
    override def defaultValue = UserType.Normal
    override def dbColumnName = "user_type"
  }

  object enabled extends MappedEnum(this, UserStatus) {
    override def defaultValue = UserStatus.Normal
  }

  object address extends MappedString(this, 250) {
    override def dbColumnName = "address"
  }

  object descn extends MappedText(this)

  object remark extends MappedString(this, 300)

  object lastLoginTime extends MappedDateTime(this)

  object loginTime extends MappedDateTime(this)

  override lazy val createdAt = new MyCreatedAt(this) {
    override def dbColumnName = "created_at"

    override def format(d: java.util.Date): String = WebHelper.fmtDateStr(d)

    override def parse(s: String): Box[java.util.Date] = {
      val df = new SimpleDateFormat("yyyy-MM-dd HH:mm")
      try {
        val date = df.parse(s)
        Full(date)
      } catch {
        case _: Exception => Full(this.set(null))
      }
    }
  }

  override lazy val updatedAt = new MyUpdatedAt(this) {
    override def dbColumnName = "updated_at"

    override def format(d: java.util.Date): String = WebHelper.fmtDateStr(d)

    override def parse(s: String): Box[java.util.Date] = {
      val df = new SimpleDateFormat("yyyy-MM-dd HH:mm")
      try {
        val date = df.parse(s)
        Full(date)
      } catch {
        case _: Exception => Full(this.set(null))
      }
    }
  }

  def displayName = if (name.get.isEmpty()) mobile.get else name.get
  def displayInfo = if (name.get.isEmpty()) <span>{ mobile.get }-{ id.get }</span> else <span>{ name.get }-{ mobile.get }-{ id.get }</span>
  def displaySuper = if (superUser.get) <span class="badge badge-success">是</span> else <span class="badge badge-important">否</span>

  object srcId extends MappedLong(this) {
    override def dbColumnName = "src_id"
  }

  def authSmsCodeOrPwd(inputCode: String) = {
    val code = SmsHelper.smsCode(mobile.get)._1
    val checkRet = (!code.trim.isEmpty() && code == inputCode) || password.match_?(inputCode)
    if (checkRet) {
      MemcachedHelper.delete(mobile.get)
    }
    checkRet
  }
}

object User extends User with MetaMegaProtoUser[User] with Paginator[User] {
  override def dbTableName = "users"

  override def fieldOrder = List(id, email, name, gender, mobile, phone, qq, userType, enabled, address, locale, timezone, password, lastLoginTime, loginTime)

  override def signupFields: List[FieldPointerType] = List(mobile, password)
}
