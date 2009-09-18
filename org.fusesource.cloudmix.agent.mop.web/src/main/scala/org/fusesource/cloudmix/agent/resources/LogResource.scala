package org.fusesource.cloudmix.agent.resources

import com.sun.jersey.api.view.ImplicitProduces
import java.io.{FileReader, BufferedReader, File}
import org.fusesource.cloudmix.agent.logging.{LogRecord, LogHandler}

/**
 * Represents a log file
 *
 * @version $Revision : 1.1 $
 */
@ImplicitProduces(Array("text/html;qs=5"))
class LogResource(file: File, parentPath: String) extends FileSystemResource(file, parentPath) {
  def isDirectory = false

  def icon: String = iconPrefix + "text.gif"


  def logRecords = {
    val handler = new LogHandler(new BufferedReader(new FileReader(file)));

    // TODO create a critera from query arguments!
    handler.getAllRecords()
  }
}