package org.fusesource.cloudmix.agent.resources

import org.fusesource.cloudmix.scalautil.Logging
import javax.ws.rs.core.{Context, UriInfo}
import java.io.File

/**
 * @version $Revision : 1.1 $
 */
abstract class FileSystemResource(val file: File, val parentPath: String) extends Logging {
  @Context
  var uriInfo: UriInfo = _;
  val iconPrefix = "/images/files/"

  def name = file.getName

  def fullname = file.toString

  def parentLink = parentPath

  def isDirectory

  def icon: String

  def path: String = {
    if (uriInfo != null) {
      uriInfo.getPath
    }
    else {
      parentPath + "/" + file.getName
    }
  }

}