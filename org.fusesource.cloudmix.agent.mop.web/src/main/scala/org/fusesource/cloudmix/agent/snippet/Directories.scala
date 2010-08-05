package org.fusesource.cloudmix.agent.snippet

import org.fusesource.cloudmix.scalautil.TextFormatting._

import com.sun.jersey.lift.ResourceBean
import net.liftweb.util.Helpers._
import scala.xml._
import scala.collection.JavaConversions._
import java.io.File
import org.fusesource.cloudmix.agent.resources.{FileSystemResource, DirectoryResource}

/**
 * @version $Revision : 1.1 $
 */

object Directories {
  def action(file: FileSystemResource) = {
    if (file.name.endsWith(".log"))
      <a href={file.path + "/log"} title="View and search the log contents Log">View Log</a>
    else
      Text("")
  }
}

import Directories._

class Directories {
  def index(xhtml: Group): NodeSeq = {
    ResourceBean.get match {
      case dir: DirectoryResource =>

        bind("directory", xhtml,
          "name" -> asText(dir.name),
          "path" -> asText(dir.path),
          "fullname" -> asText(dir.fullname),
          AttrBindParam("parentLink", Text(dir.parentLink), "href"),

          "child" -> dir.getChildResources.flatMap {
            //case (key : String, value : String) =>
            case child: FileSystemResource =>
              bind("child", chooseTemplate("directory", "child", xhtml),
                "name" -> asText(child.name),
                "action" -> action(child),
                AttrBindParam("link", Text(child.path), "href"),
                AttrBindParam("iconSource", Text(child.icon), "src"))
          }.toSeq
        )

      case _ =>
        <p>
          <b>Warning</b>
          No Directory resources found!</p>
    }
  }
}