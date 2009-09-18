package org.fusesource.cloudmix.agent.snippet

import _root_.org.fusesource.cloudmix.scalautil.TextFormatting._

import _root_.com.sun.jersey.lift.ResourceBean
import _root_.net.liftweb.util.Helpers._
import _root_.scala.xml._
import _root_.scala.collection.jcl.Conversions._
import java.io.File
import org.fusesource.cloudmix.agent.resources.{LogResource, FileSystemResource, DirectoryResource}
import org.fusesource.cloudmix.agent.logging.LogRecord

/**
 * @version $Revision : 1.1 $
 */
class Logs {
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
  }}