import ammonite.ops._

import $ivy.`com.atlassian.commonmark:commonmark:0.5.1`
import org.commonmark.html.HtmlRenderer
import org.commonmark.node._
import org.commonmark.parser.Parser
import org.commonmark.Extension._

import $ivy.`com.atlassian.commonmark:commonmark-ext-yaml-front-matter:0.9.0`
import org.commonmark.ext.front.matter._

import java.util.{Collections, List, Map, Set, Calendar}
import java.text.SimpleDateFormat

/*
 * StringUtils
 */
object StringUtils {
  def mdNameToHtml(name: String) = name.stripSuffix(".md").replace(" ", "-").toLowerCase + ".html"

  def mdFilenameToTitle(name: String) = name.replace("_", " ")
} // End of StringUtils

/*
 * DateUtils
 */
object DateUtils {
  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
  val yearMonthFormatter = new SimpleDateFormat("yyyy-MMMM")
  val monthYearFormatter = new SimpleDateFormat("MMMM yyyy")

  val today = dateFormatter.format(Calendar.getInstance().getTime)

  def dateToYearMonth(date: String) =
    yearMonthFormatter.format(dateFormatter.parse(date))

  def yearMonthToMonthYear(date: String) =
    monthYearFormatter.format(yearMonthFormatter.parse(date))
} // End of DateUtils

/*
 * ContentUtils
 */
object ContentUtils {
  // Commonmark Extensions
  val EXTENSIONS = Collections.singleton(YamlFrontMatterExtension.create())
  val PARSER = Parser.builder().extensions(EXTENSIONS).build()
  val RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).build()

  // extractContent - get the entire content without the metadata section
  def extractContent(mdContent: String): String = {
    val document = PARSER.parse(mdContent)
    val output = RENDERER.render(document)
    return output
  }

  // extractPreview - get the 20% of the entire content
  def extractPreview(mdContent: String): String = {
    val document = PARSER.parse(mdContent)
    val output = RENDERER.render(document)
    output.substring(0, (output.size / 100.0 * 20).toInt)
  } // End of extractPreview

  // extractMetadata
  def extractMetadata(mdContent: String): Map[String, List[String]] = {
    val visitor = new YamlFrontMatterVisitor()
    val document: Node = PARSER.parse(mdContent)
    document.accept(visitor)

    return visitor.getData()
  } // End of extractMetadata

} // End of ContentUtils
