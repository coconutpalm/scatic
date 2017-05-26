// Generator.sc

import ammonite.ops._

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.immutable.TreeMap
import scala.collection.mutable.ListBuffer
import java.util.{List => JList, Map => JMap}

import $ivy.`com.lihaoyi::scalatags:0.6.5`
import scalatags.Text.all._
import scalatags.Text.tags2.{title, nav}

import $file.ConfigParser, ConfigParser._
import $file.Partials, Partials.PartialFrags._
import $file.Utils, Utils._
import $file.pidhos, pidhos._

/*
 * ScaticGenerator
 */
case class ScaticGenerator(val b2conf: SiteGenConf) {

  val IndexFilename = b2conf.site.indexfile
  val GroupedByTagFilename = b2conf.site.groupedbytagfile
  val GroupedByCategoryFilename = b2conf.site.groupedbycategoryfile
  val ResourcesFolder = b2conf.directories.resources
  val OutputFolder = b2conf.directories.output
  val ContentFolder = b2conf.directories.content
  val StaticsFolder = b2conf.directories.statics

  var categoriesListBuffer = new ListBuffer[String]()
  var tagsListBuffer = new ListBuffer[List[String]]()

  def generate() = {
    // Site generation starts here
    println("\n* Generating site...")

    println("\n* Reading existing posts...")
    val postList: List[BlogPost] = _getPostList(cwd/ResourcesFolder/ContentFolder)
    println(s"\n ** ${postList.size} posts ready")

    // generate and save post pages
    println("\n* Creating 'pages' for posts...")
    _savePostPages(postList)

    // generate and save the index page
    val groupByMonth = postList.groupBy {
      case post: BlogPost => DateUtils.dateToYearMonth(post.date)
    }
    println("\n* Creating 'index' page...")
    write(cwd/OutputFolder/IndexFilename, _createPageIndex(groupByMonth))

    // generate and save the byCategory page
    val groupByCategory =  postList.groupBy(_.category)
    println("\n* Creating 'byCategory' page...")
    write(cwd/OutputFolder/GroupedByCategoryFilename, _createPageGroupedByCategory(groupByCategory))

    // generate and save the byTag page
    val groupByTag =  postList.flatMap(bp => bp.tags.map(tag => (tag, bp)))
                              .groupBy(_._1)
                              .mapValues(_.map(_._2))
    println("\n* Creating 'byTag' page...")
    write(cwd/OutputFolder/GroupedByTagFilename, _createPageGroupedByTag(groupByTag))

    // copy statics (css and javascript files to the output folder)
    println(s"""\n* Saving files to "${ContentFolder}" folder ...""")
    println(s"""\n* Saving statics files to "$OutputFolder" folder...""")
    cp(cwd/ResourcesFolder/StaticsFolder/"scatic.css", cwd/OutputFolder/"scatic.css")
    cp(cwd/ResourcesFolder/StaticsFolder/"scatic.js", cwd/OutputFolder/"scatic.js")
  }

  /***************** private methods section *****************/

  // _pageLayout
  private[this] def _pageLayout(blogHeadLink: Seq[Modifier], content: Seq[Modifier]) = {
    "<!DOCTYPE html>" + html(
      head(title(b2conf.site.title), stylesheets, blogStyle),
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
            div(cls := "col-sm-3 col-sm-offset-1 blog-sidebar", sidebar(categoriesListBuffer, tagsListBuffer))
          )
        ),
        footer(cls := "blog-footer", footerContent),
        jsList
      )
    )
  } // End of _pageLayout

  private[this] def _addToCategoriesBuffer(value: String) = categoriesListBuffer += value
  private[this] def _addToTagsBuffer(value: List[String]) = tagsListBuffer += value

  // _getPostList
  private[this] def _getPostList(pathToPosts: Path): List[BlogPost] = {
    val postFiles = ls! pathToPosts
    val unsortedPosts = for(path <- postFiles.toList) yield {
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
      val tagsList = metadata.get("tags").asScala.toList

      _addToCategoriesBuffer(category)
      _addToTagsBuffer(metadata.get("tags").asScala.toList)

      BlogPost(title, date, preview, content, htmlFilename, category, tagsList)
    }
    unsortedPosts.sortBy(_.date).reverse
  } // End of _getPostList

  // _savePostPages and save to files
  private[this] def _savePostPages(postList:  List[BlogPost]) = for(post <- postList) {
    write( cwd/OutputFolder/ContentFolder/post.htmlFilename, _createSinglePostPage(post) )
  } // End of _savePostPages

  // _createSinglePostPage
  private[this] def _createSinglePostPage(post: BlogPost) = {
    val htmlContent = Seq(
      div(cls := "blog-post",
        h3(cls := "blog-post-title",post.title),
        p(cls := "blog-post-category", "Category: ", a(post.category, href := s"/byCategory.html#${post.category}", onclick := s"""filterBy("${post.category}");""")),
        p(raw(post.content)),
        br,
        div(id := "tags-list",
          ul( "Tags: ", for(tag <- post.tags) yield li(a(tag, href := s"/byTag.html#$tag", onclick := s"""filterBy("$tag");""")) )
        ),
        br,
        hr,
        a(cls := "glyphicon glyphicon-home" ,href := "/")
      )
    )

    _pageLayout(
      Seq(h2(cls := "blog-title", a(b2conf.site.title, href := "/"))),
      htmlContent
    )
  } // End of _createSinglePostPage

  // getSortedPostListForIndexPage
  private[this] def _getSortedPostListForIndexPage(groupByMonth:  Map[String, List[BlogPost]]) = {
    val g = groupByMonth.map {
      case (yearMonth, postList) => (yearMonth, postList.map {
        case post: BlogPost =>
          Seq(
          div(cls := "blog-post",
            h3(a(post.title, href := (ContentFolder + "/" + post.htmlFilename))),
            p(cls := "blog-post-category", "Category: " + post.category, attr("data-id") := post.category),
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

  // _createPageIndex
  private[this] def _createPageIndex(groupByMonth:  Map[String, List[BlogPost]]) = {
    _pageLayout(
      Seq(h2(cls := "blog-title", b2conf.site.title)),
      _getSortedPostListForIndexPage(groupByMonth)
    )
  } // End of _createPageIndex

  // _getPostsByTagPage
  private[this] def _getPostsForByCategoryPage(groupByTag:  Map[String, List[BlogPost]]) = {
    val g = groupByTag.map {
      case (tag, postList) => (tag, postList.map {
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
      case (category, postList) =>
        div(cls :="blog-list-container hidden", id := s"$category-container",
          div(cls:= "panel panel-default",
            div(cls:= "panel-heading",
               h3(cls:= "panel-title", category + "  [#" + postList.size + "]")
            ),
            div(cls:="panel-body", postList)
          )
        )
      }.toList
  } // End of _getPostsByTagPage

  // _generateGroupByTagPage
  private[this] def _createPageGroupedByCategory(groupByCategory:  Map[String, List[BlogPost]]) = {
    _pageLayout(
      Seq(h2(cls := "blog-title", a(b2conf.site.title, href := "/"))),
      Seq(h3("Posts by Category" ), _getPostsForByCategoryPage(groupByCategory))
    )
  } // End of _generateGroupByTagPage

  // _getPostsByTagPage
  private[this] def _getPostsForByTagPage(groupByTag:  Map[String, List[BlogPost]]) = {
    val g = groupByTag.map {
      case (tag, postList) => (tag, postList.map {
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
      case (tag, postList) =>
        div(cls :="blog-list-container hidden", id := s"$tag-container",
          div(cls:= "panel panel-default",
            div(cls:= "panel-heading",
               h3( cls:= "panel-title", tag + "  [#" + postList.size + "]")
            ),
            div(cls:="panel-body", postList)
          )
        )
      }.toList
  } // End of _getPostsByTagPage

  // _generateGroupByTagPage
  private[this] def _createPageGroupedByTag(groupByTag:  Map[String, List[BlogPost]]) = {
    _pageLayout(
      Seq(h2(cls := "blog-title", a(b2conf.site.title, href := "/"))),
      Seq(h3("Posts by Tag" ), _getPostsForByTagPage(groupByTag))
    )
  } // End of _generateGroupByTagPage


} // End of ScaticGenerator
