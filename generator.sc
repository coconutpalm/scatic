import ammonite.ops._

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.immutable.TreeMap

import $ivy.`com.lihaoyi::scalatags:0.6.5`
import scalatags.Text.all._
import scalatags.Text.tags2.{title, nav}

import $file.partials, partials.Partials._
import $file.utils, utils._

case class Directories(content: String, resources: String, output: String, statics: String)
case class Site(title: String, description: String, indexfile: String, lastmod: String)
case class HttpServer(port: Int, webroot: String)
case class SiteGenConf(directories: Directories, site: Site, server: HttpServer)

/*
 * SiteGenConfigParser
 */
object SiteGenConfigParser {

  def parseOrExit(pathToConfigFile: Path): Option[SiteGenConf] = {
    if (!(exists! pathToConfigFile)) {
      println(s"Cannot find a configuration file $pathToConfigFile")
      System.exit(-1)
    }

    try {
      val confStr = read(pathToConfigFile).toString
      val res = upickle.json.read(confStr)

      val directories = (Directories.apply _).tupled(_decodeDirectories(res))
      val site        = (Site.apply _).tupled(_decodeSite(res))
      val httpServer  = (HttpServer.apply _).tupled(_decodeHttpServer(res))

      Some(new SiteGenConf(directories, site, httpServer))
    } catch {
      case e: Exception => println(e)
      None
    }
  } // End of parseOrExit

  private[this] def _decodeDirectories(jsonStr: upickle.Js.Value): Tuple4[String, String, String, String] = {
    ( jsonStr("directories")("content").toString.replace("\"", ""),
      jsonStr("directories")("resources").toString.replace("\"", ""),
      jsonStr("directories")("output").toString.replace("\"", ""),
      jsonStr("directories")("statics").toString.replace("\"", "") )
  }

  private[this] def _decodeSite(jsonStr: upickle.Js.Value): Tuple4[String, String, String, String] = {
    ( jsonStr("site")("title").toString.replace("\"", ""),
      jsonStr("site")("description").toString.replace("\"", ""),
      jsonStr("site")("indexfile").toString.replace("\"", ""),
      jsonStr("site")("lastmod").toString.replace("\"", "") )
  }

  private[this] def _decodeHttpServer(jsonStr: upickle.Js.Value): Tuple2[Int, String] = {
    ( jsonStr("server")("port").toString.replace("\"", "").toInt,
      jsonStr("server")("webroot").toString.replace("\"", "") )
  }
}

case class BlogPost(
  title: String,
  date: String,
  preview: String,
  content: String,
  htmlFilename: String,
  category: String,
  tags: String
)

/*
 * StaticSiteGenerator
 */
case class StaticSiteGenerator(val b2conf: SiteGenConf) {

  val IndexFilename = b2conf.site.indexfile
  val ResourcesFolder = b2conf.directories.resources
  val OutputFolder = b2conf.directories.output
  val ContentFolder = b2conf.directories.content
  val StaticsFolder = b2conf.directories.statics

  def generate() = {
    // Site generation starts here
    println("\n* Generating site...")

    println("\n* Reading existing posts...")
    val postList = _getPostList(cwd/ResourcesFolder/ContentFolder)
    println(s"\n ** ${postList.size} posts founded...")

    val groupByMonth = postList.groupBy {
      case post: BlogPost => DateUtils.dateToYearMonth(post.date)
    }

    // generate and save the index page
    println("\n* Generating index page...")
    write(cwd/OutputFolder/IndexFilename, _generateIndexPage(groupByMonth))
    println(s"""\n* Saving files to "${ContentFolder}" folder ...""")

    println("\n* Generating html pages for posts...")
    _generatePages(postList)

    // copy statics (css and javascript files to the output folder)
    println(s"""\n* Saving statics files to "${OutputFolder/StaticsFolder}" folder...""")
    cp(cwd/ResourcesFolder/StaticsFolder, cwd/OutputFolder/StaticsFolder)
  }

  /***************** private methods section *****************/

  // _pageLayout
  private[this] def _pageLayout(header: Seq[Modifier], blogHeadLink: Seq[Modifier], content: Seq[Modifier]) = {
    "<!DOCTYPE html>" + html(
      header,
      body(
        div(cls := "blog-masthead",
          div(cls := "container",
            nav(cls := "blog-nav", topMenuItems)
          )
        ),
        div(cls := "container",
          div(cls := "blog-header",
            blogHeadLink,
            p(cls := "lead blog-description", b2conf.site.description)
          ),
          div(cls := "row",
            div(cls := "col-sm-8 blog-main",
              content
            ),
            div(cls := "col-sm-3 col-sm-offset-1 blog-sidebar", sidebar)
          )
        ),
        footer(cls := "blog-footer", footerContent),
        jqueryJs,
        bootstrapJs
      )
    )
  } // End of _pageLayout

  // _getpostList
  private[this] def _getPostList(pathToPosts: Path): Seq[BlogPost] = {
    val postFiles = ls! pathToPosts
    val unsortedPosts = for(path <- postFiles) yield {
      val Array(date, filename, _) = path.last.split("\\.")
      val mdContent = read! path
      val metadata = ContentUtils.extractMetadata(mdContent)
      val title = metadata.get("title")(0)
      val pubDate = metadata.get("date")(0)
      assert(date == pubDate)
      val preview = ContentUtils.extractPreview(mdContent)
      val content = ContentUtils.extractContent(mdContent)
      val htmlFilename = StringUtils.mdNameToHtml(filename)
      val category = metadata.get("category")(0)
      val tags = metadata.get("tags").mkString(", ")
      //val tags = metadata.get("tags").asScala.toList
      //println(tags)

      BlogPost(title, date, preview, content, htmlFilename, category, tags )
    }
    unsortedPosts.sortBy(_.date).reverse
  } // End of _getpostList

  // getSortedPostListForIndexPage
  private[this] def getSortedPostListForIndexPage(groupByMonth:  Map[String, Seq[BlogPost]]) = {
    val g = groupByMonth.map {
      case (yearMonth, postList) => (yearMonth, postList.map {
        case post: BlogPost =>
          Seq(
            div(cls := "blog-post",
            h3(a(post.title, href := (ContentFolder + "/" + post.htmlFilename))),
            p(cls := "blog-post-category", "Category: " + post.category),
            p(raw(post.preview)),
            a(cls := "btn btn-primary btn-sm", "Read More", href := (ContentFolder + "/" + post.htmlFilename))
          ),
          hr
        )
      })
    }

    TreeMap(g.toArray: _*)(implicitly[Ordering[String]].reverse).map {
      case (yearMonth, postList) =>
        div(cls:= "panel panel-default",
          div(cls:= "panel-heading",
             h3(cls:= "panel-title", DateUtils.yearMonthToMonthYear(yearMonth) + "  [#" + postList.size + "]")
          ),
          div(cls:="panel-body", postList)
        )
      }.toList
  } // End of getSortedPostListForIndexPage

  private[this] def _generateIndexPage(groupByMonth:  Map[String, Seq[BlogPost]]) = {
    _pageLayout(
      Seq(head(title(b2conf.site.title), stylesheets, blogStyleHome)),
      Seq(h2(cls := "blog-title", b2conf.site.title)),
      getSortedPostListForIndexPage(groupByMonth)
    )
  }

  // _generatePages and save to files
  private[this] def _generatePages(sortedPosts:  Seq[BlogPost]) = for(post <- sortedPosts) {
    write( cwd/OutputFolder/ContentFolder/post.htmlFilename, _generateSinglePage(post) )
  } // End of _generatePages

  // _generateSinglePage
  private[this] def _generateSinglePage(post: BlogPost) = {
    val htmlContent = Seq(
      div(cls := "blog-post",
        h3(cls := "blog-post-title",post.title),
        p(cls := "blog-post-category", "Category: " + post.category),
        p(raw(post.content)),
        br,
        p(cls := "blog-post-meta", "Tags: [" + post.tags + "]"),
        br,
        hr,
        a(cls := "glyphicon glyphicon-home" ,href := "/")
      )
    )

    _pageLayout(
      Seq(head(title(b2conf.site.title), stylesheets, blogStylePosts)),
      Seq(h2(cls := "blog-title", a(b2conf.site.title, href := "../" + IndexFilename))),
      htmlContent
    )
  } // End of _generatePage

} // End of StaticSiteGenerator
