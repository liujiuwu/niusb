package code.snippet.admin

import code.snippet.SnippetHelper
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.util.Helpers.strToCssBindPromoter
import code.lib.WebCacheHelper
import net.liftweb.actor.LAScheduler
import net.liftweb.http.SHtml
import com.niusb.util.BootBoxHelpers

object IndexOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "actions" => actions
  }

  def actions = {
    "#reload-btn [onclick]" #> SHtml.ajaxInvoke(() => {
      LAScheduler.execute(() => WebCacheHelper.load(true))
      BootBoxHelpers.BoxAlert("重载缓存操作成功！")
    })
  }

}