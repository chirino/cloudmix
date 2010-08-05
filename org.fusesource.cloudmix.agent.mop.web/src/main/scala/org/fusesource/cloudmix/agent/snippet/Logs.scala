package org.fusesource.cloudmix.agent.snippet

import org.fusesource.cloudmix.scalautil.TextFormatting._

import scala.xml._
import scala.collection.JavaConversions._
import java.io.File
import org.fusesource.cloudmix.agent.resources.{LogResource, FileSystemResource, DirectoryResource}
import org.fusesource.cloudmix.agent.logging.LogRecord

/**
 * @version $Revision : 1.1 $
 */
class Logs {
/*
 def index(xhtml: Group): NodeSeq = {
    ResourceBean.get match {
      case resource: LogResource =>

        bind("log", xhtml,
          "name" -> asText(resource.name),
          "path" -> asText(resource.path),
          "fullname" -> asText(resource.fullname),
          AttrBindParam("parentLink", Text(resource.parentLink), "href"),

          "record" -> resource.logRecords.flatMap {
            case record: LogRecord =>
              bind("record", chooseTemplate("log", "record", xhtml),
                "category" -> asText(record.getCategory),
                "classLineNumber" -> asText(record.getClassLineNumber),
                "className" -> asText(record.getClassName),
                "date" -> asText(record.getDate),
                "level" -> asText(record.getLevel),
                "message" -> asText(record.getMessage),
                "thread" -> asText(record.getThreadId))
          }.toSeq
        )

      case _ =>
        <p>
          <b>Warning</b>
          No Directory resources found!</p>
    }
  }
*/
}