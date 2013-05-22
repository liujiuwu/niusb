package code.model

import net.liftweb.mapper._

object UserType extends Enumeration {
  type UserType = Value
  val Normal = Value(0, "普通会员")
  val Vip = Value(1, "Vip会员")
}

class User extends MegaProtoUser[User] with CreatedUpdated {
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

  object phone extends MappedString(this, 15) {
    override def dbColumnName = "phone"
  }

  object qq extends MappedString(this, 15) {
    override def dbColumnName = "qq"
  }

  object userType extends MappedEnum(this, UserType) {
    override def defaultValue = UserType.Normal
    override def dbColumnName = "user_type"
  }

  object enabled extends MappedBoolean(this) {
    override def defaultValue = true
  }

  object address extends MappedString(this, 250) {
    override def dbColumnName = "address"
  }

  object lastLoginTime extends MappedDateTime(this)

  object loginTime extends MappedDateTime(this)

  def brands = Brand.findAll(By(Brand.userId, id.is))
}

object User extends User with MetaMegaProtoUser[User] {
  override def dbTableName = "users"

  override def fieldOrder = List(id, email, name, gender, mobile, phone, qq, userType, enabled, address, locale, timezone, password, lastLoginTime, loginTime)

  override def signupFields: List[FieldPointerType] = List(mobile, password)
}
