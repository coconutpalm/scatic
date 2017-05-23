import scala.collection.mutable.ListBuffer

import scalatags.Text.all._
import scalatags.Text.tags2.nav

import $file.utils, utils.ContentUtils

object Partials {

  val jqueryJs = script(`type` := "text/javascript", src := "https://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js")

  val stylesheets = List(
    link(rel := "stylesheet", href := "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"),
    link(rel := "stylesheet", href := "https://maxcdn.bootstrapcdn.com/font-awesome/4.6.3/css/font-awesome.min.css")
  )

  val bootstrapJs = script(`type` := "text/javascript", src := "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js")

  val blogStyleHome = link(rel := "stylesheet", href := "statics/css/blog.css")
  val blogStylePosts = link(rel := "stylesheet", href := "../statics/css/blog.css")

  val topMenuItems =  Seq[Frag] (
    a(cls := "blog-nav-item active", href := "/","HOME")
  )

  val elsewhereLinks = Seq[Frag] (
    h4("Reach me on:"),
    ol(cls := "list-unstyled",
      li(a(i(cls := "fa fa-github-square"), " GitHub", href := "#", target := "_blank")),
      li(a(i(cls := "fa fa-linkedin-square"), " Linkedin", href := "#", target := "_blank")),
      li(a(i(cls := "fa fa-twitter-square"), " Twitter", href := "#", target := "_blank"))
    )
  )

  private[this] def categoriesCounterFrag(categoriesListBuffer: ListBuffer[String]) = {
    val values = ContentUtils.buildMetadataMap(categoriesListBuffer.toList).toSeq
    Seq[Frag](
      h4("Categories:"),
      ol(cls := "list-unstyled", for( (cat, counter) <- values) yield
        li(a(s"$cat ($counter)", href := "#"))
      )
    )
  }

  private[this] def tagsCounterFrag(tagsListBuffer: ListBuffer[List[String]]) = {
    val values = ContentUtils.buildMetadataMap(tagsListBuffer.toList.flatten).toSeq
    Seq[Frag](
      h4("Tags:"),
      ol(cls := "list-unstyled", for( (tag, counter) <- values) yield
        li(a(s"$tag ($counter)", href := "#"))
      )
    )
  }

  def sidebar(categoriesListBuffer: ListBuffer[String], tagsListBuffer: ListBuffer[List[String]]) = {
    Seq[Frag] (
      div(cls := "sidebar-module sidebar-module-inset",
        h4("About"),
        p("Etiam porta",
          em("sem malesuada magna"),
          "mollis euismod. Cras mattis consectetur purus sit amet fermentum. Aenean lacinia bibendum nulla sed consectetur."
        )
      ),
      div(cls := "sidebar-module", elsewhereLinks),
      div(cls := "sidebar-module", categoriesCounterFrag(categoriesListBuffer)),
      div(cls := "sidebar-module", tagsCounterFrag(tagsListBuffer))
    )
  }

  val footerContent = Seq[Frag] (
    p("Built using ",
      a("Scala", href := "http://www.scala-lang.org/"),
      a(", Ammonite", href := "https://github.com/lihaoyi/Ammonite"),
      " and ",
      a("Bootstrap", href :="http://getbootstrap.com"),
      " using a modified theme based on the ",
      a("Blog Theme", href:= "http://getbootstrap.com/examples/blog/"),
      " by ",
      a("@mdo", href := "https://twitter.com/mdo"),
      "."
    ),
    p(
      a(cls := "glyphicon glyphicon-triangle-top", href := "#"," BackTop")
    )
  )

}
