package code.snippet.admin

import java.io.File
import scala.collection.mutable.ArrayBuffer
import scala.xml._
import org.apache.commons.io.FileUtils
import code.model.Brand
import code.model.BrandStatus
import code.model.BrandType
import code.snippet.SnippetHelper
import net.liftweb.common._
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import code.model.ArticleType
import code.model.UserType
import code.model.Article
import code.model.ArticleStatus
import com.niusb.util.WebHelpers._

object ArticleOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "create" => create
    case "list" => list
    case "edit" => edit
  }

  private def bies: List[QueryParam[Article]] = {
    val (articleType, keyword, status) = (S.param("type"), S.param("keyword"), S.param("status"))
    val byBuffer = ArrayBuffer[QueryParam[Article]](OrderBy(Article.articleOrder, Descending))
    keyword match {
      case Full(k) if (!k.trim().isEmpty()) =>
        val kv = k.trim()
        byBuffer += Like(Article.title, s"%${kv}%")
      case _ =>
    }

    articleType match {
      case Full(s) if (s != "all") =>
        byBuffer += By(Article.articleType, ArticleType(s.toInt))
      case _ =>
    }

    status match {
      case Full(s) if (s != "all") =>
        byBuffer += By(Article.status, ArticleStatus(s.toInt))
      case _ =>
    }

    byBuffer.toList
  }

  def list = {
    def actions(article: Article): NodeSeq = {
      <a href={ "/admin/article/edit?id=" + article.id.get } class="btn btn-info"><i class="icon-edit"></i></a> ++ Text(" ") ++
        a(() => {
          BoxConfirm("确定删除【" + article.title.get + "】？此操作不可恢复，请谨慎！", {
            ajaxInvoke(() => { article.delete_!; JsCmds.Reload })._2
          })
        }, <i class="icon-trash"></i>, "class" -> "btn btn-danger")
    }

    val (articleType, keyword, status) = (S.param("type"), S.param("keyword"), S.param("status"))
    var url = originalUri
    var articleTypeVal, keywordVal, statusVal = ""
    articleType match {
      case Full(t) =>
        articleTypeVal = t
        url = appendParams(url, List("type" -> t))
      case _ =>
    }
    keyword match {
      case Full(k) if (!k.trim().isEmpty()) =>
        keywordVal = k
        url = appendParams(url, List("keyword" -> k))
      case _ =>
    }
    status match {
      case Full(s) =>
        statusVal = s
        url = appendParams(url, List("status" -> s))
      case _ =>
    }

    val paginatorModel = Article.paginator(url, bies: _*)()

    val searchForm = "#searchForm" #>
      <form class="form-inline" action={ url } method="get">
        <input type="text" id="keyword" name="keyword" class="span8" value={ keywordVal } placeholder="搜索标题关键词"/>
        <select id="type" name="type">
          { for ((k, v) <- Article.validArticleTypeSelectValues) yield <option value={ k } selected={ if (articleTypeVal == k) "selected" else null }>{ v }</option> }
        </select>
        <select id="status" name="status">
          { for ((k, v) <- Article.validStatusSelectValues) yield <option value={ k } selected={ if (statusVal == k) "selected" else null }>{ v }</option> }
        </select>
        <button type="submit" class="btn"><i class="icon-search"></i> 搜索</button>
        <a href="/admin/article/create" class="btn btn-primary"><i class="icon-plus"></i> 发布文章</a>
      </form>

    val dataList = "#dataList tr" #> paginatorModel.datas.map(article => {
      "#title" #> article.title &
        "#articleType" #> article.articleType &
        "#articleFrom" #> article.articleFrom.get &
        "#status" #> article.status &
        "#readCount" #> article.readCount.get &
        "#articleOrder" #> article.articleOrder.get &
        "#actions" #> actions(article)
    })
    searchForm & dataList & "#pagination" #> paginatorModel.paginate _
  }

  def edit = {
    tabMenuRV(Full("edit" -> "修改文章"))

    (for {
      articleId <- S.param("id").flatMap(asLong) ?~ "文章ID不存在或无效"
      article <- Article.find(By(Article.id, articleId)) ?~ s"ID为${articleId}的文章不存在。"
    } yield {
      def process(): JsCmd = {
        article.save()
        S.redirectTo("/admin/article/index")
      }

      val articleTypes = ArticleType.values.toList
      "@title" #> text(article.title.get, article.title(_)) &
        "@article_from" #> text(article.articleFrom.get, article.articleFrom(_)) &
        "@article_type" #> selectObj[ArticleType.Value](ArticleType.values.toList.map(v => (v, v.toString)), Full(article.articleType.is), article.articleType(_)) &
        "@status" #> selectObj[ArticleStatus.Value](ArticleStatus.values.toList.map(v => (v, v.toString)), Full(article.status.is), article.status(_)) &
        "@article_order" #> text(article.articleOrder.get.toString, v => article.articleOrder(v.toInt)) &
        "@articleContent" #> textarea(article.content.get, article.content(_)) &
        "type=submit" #> ajaxSubmit("发布", process)
    }): CssSel
  }

  def create = {
    tabMenuRV(Full("plus" -> "发布文章"))

    val article = Article.create
    def process(): JsCmd = {
      article.save()
      S.redirectTo("/admin/article/index")
    }

    val articleTypes = ArticleType.values.toList
    "@title" #> text(article.title.get, article.title(_)) &
      "@article_from" #> text(article.articleFrom.get, article.articleFrom(_)) &
      "@article_type" #> selectObj[ArticleType.Value](ArticleType.values.toList.map(v => (v, v.toString)), Full(article.articleType.is), article.articleType(_)) &
      "@status" #> selectObj[ArticleStatus.Value](ArticleStatus.values.toList.map(v => (v, v.toString)), Full(article.status.is), article.status(_)) &
      "@article_order" #> text(article.articleOrder.get.toString, v => article.articleOrder(v.toInt)) &
      "@articleContent" #> textarea(article.content.get, article.content(_)) &
      "type=submit" #> ajaxSubmit("发布", process)
  }

}