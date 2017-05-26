// pidhos.sc
// plain immutable data-handling objects

case class Directories(
  content: String,
  resources: String,
  output: String,
  statics: String
)

case class Site(
  title: String,
  description: String,
  indexfile: String,
  groupedbytagfile: String,
  groupedbycategoryfile:String,
  lastmod: String
)

case class HttpServer(
  port: Int,
  webroot: String
)

case class SiteGenConf(
  directories: Directories,
  site: Site,
  server: HttpServer
)

case class BlogPost(
  title: String,
  date: String,
  preview: String,
  content: String,
  htmlFilename: String,
  category: String,
  tags: List[String]
)
