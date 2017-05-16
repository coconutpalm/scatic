// server.sc
import $ivy.`org.eclipse.jetty:jetty-server:9.4.5.v20170502`
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.{DefaultHandler, HandlerList, ResourceHandler}

case class StaticServer(val port: Int, val webroot: String) {
  val server = {
    println(s"Starting the HTTP server....")
    val jettyServer = new Server(port)
    val resource_handler = new ResourceHandler()
    resource_handler.setWelcomeFiles(Array("index.html"))
    resource_handler.setResourceBase(webroot)
    resource_handler.setDirectoriesListed(true)
    val handlers = new HandlerList()
    handlers.setHandlers(Array(resource_handler, new DefaultHandler()))
    jettyServer.setHandler(handlers)
    Some(jettyServer)
  }

  def start(): Unit = {
    try {
      server.foreach {
        s => s.start()
        println(s"The HTTP server has been started on port $port")
      }
    } catch {
    case e: Exception =>
      println("Error occurred while trying to start the HTTP server: " + e)
    }
  }

  def stop(): Unit = {
    try {
      server.foreach { s =>
        s.stop()
        println("The HTTP server has been stopped")
      }
    } catch {
      case e: Exception =>
        println("Error occurred while trying to stop the HTTP server: " +  e)
    }
  }
}
