package org.fusesource.cloudmix.agent.resources

import java.io.File
import javax.ws.rs._
import com.sun.jersey.api.view.ImplicitProduces

/**
 * Represents a directory on the local disk
 *
 * @version $Revision : 1.1 $
 */
@ImplicitProduces(Array("text/html;qs=5"))
@Produces(Array("application/xml", "application/json", "text/xml", "text/json"))
class DirectoryResource(file: File, parentPath: String) extends FileSystemResource(file, parentPath) {

  @DELETE
  def delete(): Unit = {
    log.info("Deleting directory: " + file)
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  @Path("{name}")
  def child(@PathParam("name") name: String): FileSystemResource = {
    val child = new File(file, name)
    if (child.exists) {
      createResource(child)
    }
    else {
      null
    }
  }

  def isDirectory = true

  def icon = iconPrefix + "folder.gif"


  def getChildResources = {
    getChildren.map(createResource(_))
  }

  def getChildren: Array[File] = {
    file.listFiles
  }

  def createResource(file: File) = if (file.isDirectory()) new DirectoryResource(file, path) else new FileResource(file, path)

}