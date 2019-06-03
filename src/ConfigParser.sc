// ConfigParser.sc

import ammonite.ops._
import $file.pidhos, pidhos.{Directories, Site, HttpServer, SiteGenConf}

/*
 * ScaticConfigParser
 */
object ScaticConfigParser {

  def parseOrExit(pathToConfigFile: Path): Option[SiteGenConf] = {
    if (!(exists! pathToConfigFile)) {
      println(s"Cannot find a configuration file $pathToConfigFile")
      System.exit(-1)
    }

    try {
      val confStr = read(pathToConfigFile).toString
      val res = ujson.read(confStr)

      val directories = (Directories.apply _).tupled(_decodeDirectories(res))
      val site        = (Site.apply _).tupled(_decodeSite(res))
      val httpServer  = (HttpServer.apply _).tupled(_decodeHttpServer(res))

      Some(new SiteGenConf(directories, site, httpServer))
    } catch {
      case e: Exception => println(e)
      None
    }
  } // End of parseOrExit

  private[this] def _decodeDirectories(jsonStr: ujson.Js.Value): Tuple4[String, String, String, String] = {
    ( jsonStr("directories")("content").toString.replace("\"", ""),
      jsonStr("directories")("resources").toString.replace("\"", ""),
      jsonStr("directories")("output").toString.replace("\"", ""),
      jsonStr("directories")("statics").toString.replace("\"", "") )
  }

  private[this] def _decodeSite(jsonStr: ujson.Js.Value): Tuple6[String, String, String, String, String, String] = {
    ( jsonStr("site")("title").toString.replace("\"", ""),
      jsonStr("site")("description").toString.replace("\"", ""),
      jsonStr("site")("indexfile").toString.replace("\"", ""),
      jsonStr("site")("groupedbytagfile").toString.replace("\"", ""),
      jsonStr("site")("groupedbycategoryfile").toString.replace("\"", ""),
      jsonStr("site")("lastmod").toString.replace("\"", "") )
  }

  private[this] def _decodeHttpServer(jsonStr: ujson.Js.Value): Tuple2[Int, String] = {
    ( jsonStr("server")("port").toString.replace("\"", "").toInt,
      jsonStr("server")("webroot").toString.replace("\"", "") )
  }

} // End of ScaticConfigParser
