package code.snippet

import net.liftmodules.extras.snippet.BsMenu
import scala.xml._
import net.liftweb._
import common._
import http.{ LiftRules, Req, S }
import sitemap.{ Loc, SiteMap }
import util._
import util.Helpers._
import code.model.User

object Menus extends BsMenu {

  def myGroup = {
    val menus: NodeSeq =
      for {
        group <- S.attr("group") ?~ "Group not specified"
        sitemap <- LiftRules.siteMap ?~ "Sitemap is empty"
        request <- S.request ?~ "Request is empty"
        curLoc <- request.location ?~ "Current location is empty"
      } yield ({
        val currentClass = S.attr("current_class").openOr("active")
        sitemap.locForGroup(group) flatMap { loc =>
          val nonHiddenKids = loc.menu.kids.filterNot(_.loc.hidden)
          val styles =
            if (curLoc.name == loc.name || loc.menu.kids.exists(_.loc.name == curLoc.name)) currentClass
            else ""

          if (nonHiddenKids.length == 0) {
            <li class={ styles }>{ SiteMap.buildLink(loc.name) }</li>
          } else {
            val dropdown: NodeSeq = nonHiddenKids.map { kid =>
              <li>{ SiteMap.buildLink(kid.loc.name) }</li>
            }

            <li class={ styles + " dropdown" }>
              <a href="#" class="dropdown-toggle" data-toggle="dropdown">{ loc.linkText.openOr(Text("Empty Name")) } <b class="caret"></b></a>
              <ul class="dropdown-menu">{ dropdown }</ul>
            </li>
          }
        }
      }): NodeSeq

    "span" #> menus
  }

  def userStatus = {
    val menus: NodeSeq = User.currentUser match {
      case Full(user) =>
        <li class="dropdown">
          <a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="icon-user"></i> <span id="displayName">{ user.displayName } </span><b class='caret'></b></a>
          <ul class="dropdown-menu">
            <lift:Menu.group group="userMenu">
              <li><menu:bind/></li>
            </lift:Menu.group>
            {
              if (User.superUser_?)
                <li class="last"><a href="/admin/web/set"><span><i class="icon-cogs"></i> 后台管理</span></a></li>
            }
            <li class="divider"></li>
            <li class="last"><a href="/user/sign_out"><span><i class="icon-signout"></i> 退出</span></a></li>
          </ul>
        </li>
      case _ =>
        <button class="btn btn-success" type="button" data-toggle="modal" data-target="#loginDialog">登录或注册</button>
    }

    "* *" #> menus
  }
  
  def userStatus2 = {
    val menus: NodeSeq = User.currentUser match {
      case Full(user) =>
        <div class="user-info">
          <span class="user-name"><i class="icon-user"></i> <span id="displayName">{ user.displayName } </span><b></b></span>
          <ul class="user-menu">
            <lift:Menu.group group="userMenu">
              <li><menu:bind/></li>
            </lift:Menu.group>
            {
              if (User.superUser_?)
                <li class="last"><a href="/admin/web/set"><span><i class="icon-cogs"></i> 后台管理</span></a></li>
            }
            <li class="last"><a href="/user/sign_out"><span><i class="icon-signout"></i> 退出登录</span></a></li>
          </ul>
        </div>
      case _ =>
        <button class="btn btn-success" type="button" data-toggle="modal" data-target="#loginDialog">登录或注册</button>
    }

    "* *" #> menus
  }
}