package org.fusesource.cloudmix.agent.resources

import com.google.common.base.Predicate
import com.sun.jersey.api.view.ImplicitProduces
import java.io.{FileReader, BufferedReader, File}
import java.util.List
import javax.ws.rs.{GET, Produces, Path}
import javax.ws.rs.core.{Context, UriInfo}
import org.fusesource.cloudmix.agent.logging.{LogRecordList, LogRecord, LogHandler, LogPredicate}

/**
 * Represents a log file
 *
 * @version $Revision : 1.1 $
 */
@ImplicitProduces(Array("text/html;qs=5"))
class LogResource(file: File, parentPath: String) extends FileSystemResource(file, parentPath) {
  def isDirectory = false

  def icon: String = iconPrefix + "text.gif"

  var handler: LogHandler = null
  var lock: AnyRef = new Object()

  def logRecords = {
    doLogRecords(null)
  }

  @GET
  @Produces(Array("application/xml", "application/json", "text/xml", "text/json"))
  def logRecordList(@Context ui: UriInfo) = {
    new LogRecordList(doLogRecords(ui))
  }

  def doLogRecords(ui: UriInfo) = {
    lock.synchronized {
      if (handler == null) {
        handler = new LogHandler(new BufferedReader(new FileReader(file)));
      }
    }

    val predicate = createPredicate(ui)
    if (predicate != null) {
      handler.findWithPredicate(predicate)
    } else {
      handler.getAllRecords()
    }
  }


  def createPredicate(ui: UriInfo): Predicate[LogRecord] = {
    println("UrlInfo: " + ui)

    if (ui != null) {
      val queries = ui.getQueryParameters(true)
      if (queries.isEmpty()) {
        null
      }
      val record = new LogRecord();

      val levels = queries.get("level")
      if (levels != null && !levels.isEmpty()) {
        record.setLevel(levels.get(0))
        new LogPredicate(record)
      } else {
        null
      }
    } else {
      null
    }
  }
}