package org.fusesource.cloudmix.agent.snippet

import _root_.org.fusesource.cloudmix.scalautil.TextFormatting._

import _root_.com.sun.jersey.lift.ResourceBean
import _root_.net.liftweb.util.Helpers._
import _root_.scala.xml._
import _root_.scala.collection.jcl.Conversions._
import java.io.File
import org.fusesource.cloudmix.agent.resources.{FileSystemResource, DirectoryResource}

/**
 * @version $Revision : 1.1 $
 */

object Directories {
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