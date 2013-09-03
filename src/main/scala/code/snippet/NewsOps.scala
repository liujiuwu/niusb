package code.snippet

import scala.xml.NodeSeq
import scala.xml.Text
import code.model.Brand
import code.model.User
import code.model.UserData
import com.niusb.util.WebHelpers._
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.mapper.By
import net.liftweb.util.CssSel
import net.liftweb.util.Helpers._
import com.niusb.util.SearchBrandFormHelpers
import code.model.Article
import code.model.ArticleType
import scala.xml.Unparsed

object NewsOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "index" => index
    case "view" => view
  }

  def view = {
    def info(news: Article) = {
      <lift:children><span>{ news.createdAt.asHtml }</span><span>{ news.readCount.incr(realIp).toString() }次阅读</span><span>来源：{ news.articleFrom.displayFrom }</span> </lift:children>
    }
    (for {
      id <- S.param("id").flatMap(asLong) ?~ "新闻或公告ID不存在或无效"
      news <- Article.find(By(Article.id, id), By(Article.articleType, ArticleType.News)) ?~ s"ID为${id}的新闻或公告不存在。"
    } yield {
      ".tit *" #> news.title.get &
        ".info-bar *" #> info(news) &
        ".text-ct *" #> Unparsed(news.content.get)
    }): CssSel
  }

  def index = {
    val limit = S.attr("limit").map(_.toInt).openOr(20)
    val bies = By(Article.articleType, ArticleType.News)
    val paginatorModel = Article.paginator(originalUri, bies)(itemsOnPage = limit)
    val dataList = "m-box" #> paginatorModel.datas.map(_.title.displayTitle)
    
    dataList
  }
}