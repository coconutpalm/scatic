// main.sc
import ammonite.ops._

import $file.generator, generator.{SiteGenConfigParser, StaticSiteGenerator}
import $file.server, server.StaticServer

// Remove all previous generated files
def cleanup(outputFolder: String) = {
  if(exists! cwd/outputFolder) {
    println("\n* Cleaning the output folder...")
    rm! cwd/outputFolder
  }
}

/*
 * Usage: "amm Main.sc -- --mode  generate | serve | clean"
 */
@main
def main(mode: String) = {
  println("\n**********************************")
  val confFilePath = cwd/'resources/"blogGenConf.json"
  val b2conf = SiteGenConfigParser.parseOrExit(confFilePath)

  mode match {
   case "generate" =>
      cleanup(b2conf.get.directories.output)
      StaticSiteGenerator(b2conf.get).generate()
   case "serve"    => StaticServer(b2conf.get.server.port, b2conf.get.directories.output).start()
   case "clean"    => cleanup(b2conf.get.directories.output)
  }
  println("\n**********************************\n")
}
