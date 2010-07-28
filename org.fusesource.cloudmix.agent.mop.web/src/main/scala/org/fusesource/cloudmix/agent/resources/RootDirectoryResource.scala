package org.fusesource.cloudmix.agent.resources

import java.io.File

/**
 * @version $Revision: 1.1 $
 */
class RootDirectoryResource(file: File, parentPath: String) extends DirectoryResource(file, parentPath) {
  override def path = parentPath

  override def parentLink = {
    val idx = parentPath.lastIndexOf('/')
    if (idx > 0) {
      parentPath.substring(0, idx)
    }
    else {
      "/"
    }
  }
}