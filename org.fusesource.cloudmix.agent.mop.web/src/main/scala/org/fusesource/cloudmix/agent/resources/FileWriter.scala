package org.fusesource.cloudmix.agent.resources

import java.lang.annotation.Annotation
import java.lang.{String, Class}

import javax.ws.rs.core.{MultivaluedMap, MediaType}
import javax.ws.rs.ext.{MessageBodyWriter, Provider}
import java.lang.reflect.Type
import java.io.{BufferedInputStream, FileInputStream, File, OutputStream}
import org.fusesource.scalate.util.IOUtil

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

    println("writing file: " + file + " to output")

    // TODO hacky as we read it all in RAM!


    val bytes = IOUtil.loadBinaryFile(file)
    outputStream.write(bytes)
    //outputStream.flush

    // TODO we don't want to close the stream!
    //IOUtil.copy(new BufferedInputStream(new FileInputStream(file)), outputStream)

    //StreamHelp.pump(, outputStream)
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
