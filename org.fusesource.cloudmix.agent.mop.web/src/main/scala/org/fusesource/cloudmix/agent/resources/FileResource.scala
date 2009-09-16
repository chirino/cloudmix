package org.fusesource.cloudmix.agent.resources

import java.io.File
import javax.ws.rs.core.Response.Status
import javax.ws.rs.core.Response
import javax.ws.rs.{Produces, GET, PathParam}

/**
 * @version $Revision : 1.1 $
 */
class FileResource(file: File, parentPath: String) extends FileSystemResource(file, parentPath) {

  def isDirectory = false

  def icon = {
    val n = name.toLowerCase
    if (n.endsWith(".tar") || n.endsWith(".gz") || n.endsWith(".zip")) {
      iconPrefix + "compressed.gif"
    }
    else if (n.endsWith(".doc") || n.endsWith(".pdf")) {
      iconPrefix + "layout.gif"
    }
    else if (n.endsWith(".txt") || n.endsWith(".log") || n.endsWith(".out")) {
      iconPrefix + "text.gif"
    }
    else {
      iconPrefix + "unknown.gif"
    }
  }

  @GET
  @Produces
  def contents(@PathParam("name") name: String): Response = {
    val f = file
    if (f.exists) {
      Response.ok(f).build()
    }
    else {
      Response.status(Status.NOT_FOUND).build()
    }
  }
}