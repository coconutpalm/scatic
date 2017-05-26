// Main.sc

import ammonite.ops._

import $file.src.ConfigParser, ConfigParser.ScaticConfigParser
import $file.src.Generator, Generator.ScaticGenerator
import $file.src.Server, Server.ScaticServer

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
  val b2conf = ScaticConfigParser.parseOrExit(confFilePath)

  mode match {
   case "generate" =>
      cleanup(b2conf.get.directories.output)
      ScaticGenerator(b2conf.get).generate()
   case "serve"    => ScaticServer(b2conf.get.server.port, b2conf.get.directories.output).start()
   case "clean"    => cleanup(b2conf.get.directories.output)
  }
  println("\n**********************************\n")
}
