// Files.sc
//
// See tests at the bottom for usage


import $file.Testing
import Testing._
import java.io._
import java.nio.file._
import scala.io.Source

// home is where the heart is
val home: XFile = new File(System.getProperty("user.home")) with Walking
val root: XFile = new File("/") with Walking
val pwd: XFile = new File(new File(".").getCanonicalPath) with Walking
val wd: XFile = pwd

implicit class SymbolDir(s: Symbol) { def unary_~ : XFile = home / s }
implicit class StringDir(s: String) { def unary_~ : XFile = home / s }
implicit def toXFile(f: File): XFile = new File(f.getCanonicalPath) with Walking


// Don't leak handles when reading files
def withFile[T](file: File)(f: Source => T): T = {
  import scala.util.{Try,Success,Failure}

  val s = Source.fromFile(file)

  Try(f(s)) match {
    case Success(result) => {
      s.close
      result
    }
    case Failure(e) => {
      s.close
      throw e
    }
  }
}
def withFile[T](path: String)(f: Source => T): T = withFile(new File(path))(f)


/** java.io.File += Walking */
type XFile = File with Walking

/** A predicate for filtering file names */
type FileFilter = String => Boolean
val Yes = (s: String) => true


/** Usage: new java.io.File("/some/where") with Walking */
trait Walking extends Internal.MetaPath[File] {
  override val xthis: XFile = this.asInstanceOf[XFile]

  override def newXFile(childName: String, olderSiblings: List[EnhancedUnderlying], newAncestors: List[EnhancedUnderlying]): XFile = {
    trait XWalking extends Walking {
      override val ancestors = newAncestors
      override val nextSiblings = olderSiblings
    }
    new File(xthis, childName) with XWalking
  }

  // Specialize this trait's implementation
  override def listXFiles(inclusions: FileFilter = Yes): List[XFile] = super.listXFiles(inclusions).map(_.asInstanceOf[XFile])

  /** Lazy recursive stream of all MetaPaths */
  override def walk(inclusions: FileFilter = s => true): Stream[XFile] = super.walk(inclusions).map(_.asInstanceOf[XFile])
  override def walkAll(inclusions: FileFilter = s => true): List[XFile] = walk(inclusions).toList

  // Path handling

  /** (from java.io.File) */
  def getCanonicalPath: String

  def /(sub: String): XFile = new File(getCanonicalPath + File.separator + sub) with Walking
  def /(sub: Symbol): XFile = this / sub.name

  // Get content
  def get: String = withFile(xthis) { content => content.getLines.mkString("\n") }
  def getLines: Array[String] = withFile(xthis) { content => content.getLines.toArray }

  override def toString = s"\n$getCanonicalPath${ if (isDirectory) ":\n" + list.mkString("\n") else "" }"
}


object Internal extends Testing {
  /**
   * Enhanced java.io.File-like-things
   */
  trait MetaPath[Underlying] {
    type EnhancedUnderlying = Underlying with MetaPath[Underlying]
    val xthis = this.asInstanceOf[EnhancedUnderlying]

    /** Clients should override so we know how to build their type */
    def newXFile(childName: String, olderSiblings: List[EnhancedUnderlying], ancestors: List[EnhancedUnderlying]): EnhancedUnderlying

    
    /** (from java.io.File API) */
    def isDirectory: Boolean

    /** (from java.io.File API) */
    def list: Array[String]


    /** How we get back to the top */
    val ancestors: List[EnhancedUnderlying] = Nil

    /** How we know how much farther to go at this level */
    val nextSiblings: List[EnhancedUnderlying] = Nil

    /** Like #listFiles, but with optional filtering and returning EnhancedUnderlying */
    def listXFiles(inclusions: FileFilter = s => true): List[EnhancedUnderlying] = {
      childNames2MetaPaths(xthis.list.filter(inclusions).toList)
    }

    /** The next MetaPath, depth-first style */
    def next(inclusions: FileFilter = s => true): Option[EnhancedUnderlying] = {
      def maybeNextSibling = nextSiblings match {
        case sibling :: _ => Some(sibling)
        case Nil          => ancestorNextSibling
      }

      if (isDirectory) listXFiles(inclusions) match {
        case xfile :: _ => Some(xfile)
        case Nil        => maybeNextSibling
      } else maybeNextSibling
    }

    /** Lazy recursive stream of all MetaPaths */
    def walk(inclusions: FileFilter = s => true): Stream[EnhancedUnderlying] = xthis #:: {
      next(inclusions) match {
        case None => Stream.Empty
        case Some(xfile) => xfile.walk
      }
    }

    /** Recursive file/directory list */
    def walkAll(inclusions: FileFilter = s => true): List[EnhancedUnderlying] = walk(inclusions).toList

    /* How we return to the top */
    private def ancestorNextSibling: Option[EnhancedUnderlying] = ancestors match {
      case Nil         => None
      case parent :: _ => parent.nextSiblings match {
        case sibling :: _ => Some(sibling)
        case Nil          => parent.ancestorNextSibling
      }
    }

    /* The happy family */
    private def childNames2MetaPaths(childNames: List[String]): List[EnhancedUnderlying] = childNames.toList match {
      case Nil                            => Nil
      case thisChildName :: olderChildren => {
        val olderSiblings = childNames2MetaPaths(olderChildren)
        newXFile(thisChildName, olderSiblings, xthis :: this.ancestors) :: olderSiblings
      }
    }
  }


  //=== Test infrastructure / tests ===
  
  // A dummy File-like thing
  class F(val name: String, val children: F*) {
    def isDirectory = !children.isEmpty
    def list = children.map(_.name).toArray
  }

  trait FMetaPath extends MetaPath[F] {
    override def newXFile(childName: String, olderSiblings: List[EnhancedUnderlying], newAncestors: List[EnhancedUnderlying]): EnhancedUnderlying = {
      trait XFMetaPath extends FMetaPath {
        override val ancestors = newAncestors
        override val nextSiblings = olderSiblings
      }
      new F(childName, newAncestors.head.children.find(_.name == childName).get.children: _*) with XFMetaPath
    }
  }

  type XF = F with MetaPath[F]

  def flattened(f: XF): Vector[String] = {
    def unwind(nextF: XF, fs: Vector[XF]): Vector[String] = nextF.next match {
      case Some(ff) => unwind(ff, fs ++ Vector(ff))
      case None     => fs.map( _.name )
    }
    unwind(f, Vector(f))
  }

  def f(name: String, children: F*): XF = new F(name, children: _*) with FMetaPath

  testing("Walking") {
    def nFs(n: Int): Vector[XF] = (1 to n).map(_.toString).map(f(_)).toVector

    mustBeEqual(f("foo").children, Nil)
    mustBeEqual(f("notADirectory").next, None)
    
    val twoLevels = f("top", nFs(5): _*)
    mustBeEqual(Vector("top", "1", "2", "3", "4", "5"), flattened(twoLevels))

    val threeLevels = f("root", f("sub1", nFs(5): _*), f("lonely1"), f("sub2", nFs(5): _*))
    mustBeEqual(Vector("root", "sub1") ++ nFs(5).map(_.name) ++ Vector("lonely1", "sub2") ++ nFs(5).map(_.name), flattened(threeLevels))
    mustBeEqual(List("root", "sub1") ++ nFs(5).map(_.name).toList ++ List("lonely1", "sub2") ++ nFs(5).map(_.name).toList, threeLevels.walkAll.map(_.name))
  }

  testing("Traversal and retrieval") {
    mustBeEqual((root/'usr/'bin).getCanonicalPath, new File("/usr/bin").getCanonicalPath)

    mustBeEqual((~".").getCanonicalPath, System.getProperty("user.home"))
    mustBeEqual((~'thisdirectory/'is/'guaranteed/'notTo/'exist).getCanonicalPath, System.getProperty("user.home")+"/thisdirectory/is/guaranteed/notTo/exist")
    mustBeEqual((~'thisdirectory/'is/'guaranteed/'notTo/'exist).exists, false)

    mustBeEqual((root/'etc/'passwd).getLines.isEmpty, false)
  }
}
Internal.runTests
