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
import scala.tools.scalap.scalax.util.StringUtil
import net.liftweb.util.Helpers
import net.liftweb.util.Html5
import scala.xml.XML
import com.niusb.util.WebHelpers
import net.liftweb.mapper.BySql
import net.liftweb.mapper.IHaveValidatedThisSQL

object ArticleOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "newsList" => newsList
    case "viewNews" => viewNews
    case "helpList" => helpList
  }

  def viewNews = {
    def info(news: Article) = {
      <lift:children><span>{ news.createdAt.asHtml }</span><span>{ news.readCount.incr(realIp).toString() }次阅读</span><span>来源：{ news.articleFrom.displayFrom }</span> </lift:children>
    }
    (for {
      id <- S.param("id").flatMap(asLong) ?~ "新闻或公告ID不存在或无效"
      bies = BySql[Article]("article_type=? or article_type=?", IHaveValidatedThisSQL("liujiuwu", "2013-09-14"), ArticleType.News.id, ArticleType.Notice.id)
      news <- Article.find(By(Article.id, id), bies) ?~ s"ID为${id}的新闻或公告不存在。"
    } yield {
      ".tit *" #> news.title.get &
        ".info-bar *" #> info(news) &
        ".text-ct *" #> Unparsed(news.content.get)
    }): CssSel
  }

  def newsList = {
    val limit = S.attr("limit").map(_.toInt).openOr(15)
    val bies = BySql[Article]("article_type=? or article_type=?", IHaveValidatedThisSQL("liujiuwu", "2013-09-14"), ArticleType.News.id, ArticleType.Notice.id)
    val paginatorModel = Article.paginator(originalUri, bies)(itemsOnPage = limit)
    val dataList = "#dataList li" #> paginatorModel.datas.map { news =>
      val newContent = XML.loadString({ "<b>" + news.content.get + "</b>" }).text
      val len = newContent.length()
      val fcontent = if (len > 100) newContent.substring(0, 100) + " ..." else newContent
      ".news-type *" #> news.articleType.asHtml &
        ".news-title *" #> news.title.displayTitle &
        ".news-time *" #> { "(" + news.shortCreatedAt + ")" } &
        ".news-content *" #> fcontent
    }

    dataList & "#pagination" #> paginatorModel.paginate _
  }

  def helpList = {
    val limit = S.attr("limit").map(_.toInt).openOr(15)
    val paginatorModel = Article.paginator(originalUri, By(Article.articleType, ArticleType.Help))(itemsOnPage = limit)
    val dataList = "#dataList li" #> paginatorModel.datas.map { help =>
        "#help-title" #> help.title.displayTitle &
        ".box-content *" #> Unparsed(help.content.get)
    }

    dataList & "#pagination" #> paginatorModel.paginate _
  }
}