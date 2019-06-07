// Main.sc
import $file.predef
val shellSession = ammonite.shell.ShellSession()
import shellSession._

import ammonite.ops._

import $file.src.ConfigParser, ConfigParser.ScaticConfigParser
import $file.src.Generator, Generator.ScaticGenerator
import $file.src.Server, Server.ScaticServer

// Remove all previous generated files
def cleanup(outputFolder: String) = {
  if(exists! pwd/outputFolder) {
    println("\n* Cleaning the output folder...")
    rm! pwd/outputFolder
  }
}

/*
 * Usage: "amm Main.sc -- --mode  generate | serve | clean"
 */
@main
def main(mode: String) = {
  println("\n**********************************")
  val confFilePath = pwd/'resources/"blogGenConf.json"
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

{
  case class Markdown(val file: XFile, val metadata: Map[String,String])
  case class Result(val contents: String, val destination: XFile)
  case class Site(sources: Array[Markdown] = Array(), output: Array[Result] = Array())
  
  trait Task extends AbstractTask[Site]
  
  type FileFilter = XFile => Boolean
  val noGitFiles = (f: XFile) => !f.getCanonicalPath.contains(".git")
  
  case class SourceDir(dir: XFile, sourceExtensions: String*)(implicit include: FileFilter = noGitFiles) extends Task {
    private def filter(file: XFile) = {
      val filePath = file.getCanonicalPath
      include(file) && sourceExtensions.map(filePath.endsWith(_)).foldLeft(false)(_ || _)
    }
  
    override def transform(old: Site) = {
      val additional = dir.walk.filter(filter)
      Site(old.sources ++ additional, old.output)
    }
  }
  
  val job = SourceDir(wd / 'src) compose identity[Site => Site]
  }
  
  