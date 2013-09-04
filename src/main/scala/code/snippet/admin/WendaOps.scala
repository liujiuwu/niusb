package code.snippet.admin

import scala.collection.mutable.ArrayBuffer
import scala.xml._

import com.niusb.util.WebHelpers._

import code.model.Article
import code.model.ArticleStatus
import code.model.ArticleType
import code.model.Wenda
import code.model.WendaStatus
import code.model.WendaType
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

object WendaOps extends DispatchSnippet with SnippetHelper with Loggable {
  def dispatch = {
    case "create" => create
    case "list" => list
    case "edit" => edit
  }

  private def bies: List[QueryParam[Wenda]] = {
    val (wendaType, keyword, status) = (S.param("type"), S.param("keyword"), S.param("status"))
    val byBuffer = ArrayBuffer[QueryParam[Wenda]]()
    keyword match {
      case Full(k) if (!k.trim().isEmpty()) =>
        val kv = k.trim()
        byBuffer += Like(Wenda.title, s"%${kv}%")
      case _ =>
    }

    wendaType match {
      case Full(s) if (s != "all") =>
        byBuffer += By(Wenda.wendaType, WendaType(s.toInt))
      case _ =>
    }

    status match {
      case Full(s) if (s != "all") =>
        byBuffer += By(Wenda.status, WendaStatus(s.toInt))
      case _ =>
    }

    byBuffer.toList
  }

  def list = {
    def actions(wenda: Wenda): NodeSeq = {
      <a href={ "/admin/wenda/edit?id=" + wenda.id.get } class="btn btn-info"><i class="icon-edit"></i></a> ++ Text(" ") ++
        a(() => {
          BoxConfirm("确定删除【" + wenda.title.get + "】？此操作不可恢复，请谨慎！", {
            ajaxInvoke(() => { wenda.delete_!; JsCmds.Reload })._2
          })
        }, <i class="icon-trash"></i>, "class" -> "btn btn-danger")
    }

    val (wendaType, keyword, status) = (S.param("type"), S.param("keyword"), S.param("status"))
    var url = originalUri
    var wendaTypeVal, keywordVal, statusVal = ""
    wendaType match {
      case Full(t) =>
        wendaTypeVal = t
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

    val paginatorModel = Wenda.paginator(url, bies: _*)()

    val searchForm = "#searchForm" #>
      <form class="form-inline" action={ url } method="get">
        <input type="text" id="keyword" name="keyword" class="span8" value={ keywordVal } placeholder="搜索标题关键词"/>
        <select id="type" name="type">
          { for ((k, v) <- Wenda.validWendaTypeSelectValues) yield <option value={ k } selected={ if (wendaTypeVal == k) "selected" else null }>{ v }</option> }
        </select>
        <select id="status" name="status">
          { for ((k, v) <- Wenda.validStatusSelectValues) yield <option value={ k } selected={ if (statusVal == k) "selected" else null }>{ v }</option> }
        </select>
        <button type="submit" class="btn"><i class="icon-search"></i> 搜索</button>
        <a href="/admin/wenda/create" class="btn btn-primary"><i class="icon-plus"></i> 发布问答</a>
      </form>

    val dataList = "#dataList tr" #> paginatorModel.datas.map(wenda => {
      "#title" #> wenda.title.is &
        "#wendaType" #> wenda.wendaType &
        "#status" #> wenda.status &
        "#readCount" #> wenda.readCount.is &
        "#actions" #> actions(wenda)
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
    tabMenuRV(Full("plus" -> "发布问答"))

    val wenda = Wenda.create
    def process(): JsCmd = {
      wenda.asker(0)
      wenda.reply(0)
      wenda.save()
      S.redirectTo("/admin/wenda/index")
    }

    val wendaTypes = WendaType.values.toList
    "@title" #> text(wenda.title.get, wenda.title(_)) &
      "@wenda_type" #> selectObj[WendaType.Value](WendaType.values.toList.map(v => (v, v.toString)), Full(wenda.wendaType.is), wenda.wendaType(_)) &
      "@status" #> selectObj[WendaStatus.Value](WendaStatus.values.toList.map(v => (v, v.toString)), Full(wenda.status.is), wenda.status(_)) &
      "@askContent" #> textarea(wenda.askContent.is, wenda.askContent(_)) &
      "@replyContent" #> textarea(wenda.replyContent.get, wenda.replyContent(_)) &
      "type=submit" #> ajaxSubmit("发布", process)
  }

}