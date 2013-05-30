package code.snippet

import code.model.User
import net.liftweb.common.Full
import net.liftweb.util.Helpers._
import scala.xml.NodeSeq

object NavOps {

  def userStatus = {
    val menus: NodeSeq = User.currentUser match {
      case Full(user) =>
        <li class="dropdown">
          <a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="icon-user"></i> <span id="topBarUserName">{ if (user.name.get.isEmpty()) user.mobile.get else user.name.get } </span><b class='caret'></b></a>
          <ul class="dropdown-menu">
            <lift:Menu.group group="userMenu">
              <li><menu:bind/></li>
            </lift:Menu.group>
            <li class="divider"></li>
            <li class="last"><a href="/user/sign_out"><span><i class="icon-signout"></i> 退出</span></a></li>
          </ul>
        </li>
      case _ =>
        <button class="btn btn-small btn-success" type="button" data-toggle="modal" data-target="#loginDialog">会员登录或注册</button>
    }

    "* *" #> menus
  }

}