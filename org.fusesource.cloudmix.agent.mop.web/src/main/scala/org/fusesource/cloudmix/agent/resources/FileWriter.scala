package org.fusesource.cloudmix.agent.resources

import java.lang.annotation.Annotation
import java.lang.{String, Class}

import javax.ws.rs.core.{MultivaluedMap, MediaType}
import javax.ws.rs.ext.{MessageBodyWriter, Provider}
import java.lang.reflect.Type
import java.io.{BufferedInputStream, FileInputStream, File, OutputStream}
import scalax.io.StreamHelp

/**
 * Outputs a File object over JAXRS
 *
 * @version $Revision : 1.1 $
 */
@Provider
class FileWriter extends MessageBodyWriter[File] {
  def isWriteable(aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = {
    classOf[File].isAssignableFrom(aClass)
  }

  def getSize(file: File, aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = -1L

  def writeTo(file: File, aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType, stringObjectMultivaluedMap: MultivaluedMap[String, Object], outputStream: OutputStream): Unit = {
    // TODO should we output the MIME type by guessing the file name?

    StreamHelp.pump(new BufferedInputStream(new FileInputStream(file)), outputStream)
    /*
        val in =
        var valid = true
        while (valid) {
          val c = in.read
          if (c < 0) {
            valid = false
          }
          else {
            outputStream.write(c)
          }
        }
    */
  }
}
