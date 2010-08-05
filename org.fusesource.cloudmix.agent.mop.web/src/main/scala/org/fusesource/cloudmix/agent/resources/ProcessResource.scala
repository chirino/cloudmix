package org.fusesource.cloudmix.agent.resources

import org.fusesource.cloudmix.agent.mop.{MopProcess, MopAgent}

import com.sun.jersey.api.representation.Form
import java.net.URI
import javax.ws.rs.core.MediaType._
import javax.ws.rs.core.{Response, HttpHeaders, UriInfo, UriBuilder, Context}
import javax.ws.rs.{Path, Consumes, POST, DELETE}
import org.fusesource.cloudmix.agent.snippet.Agents
import collection.JavaConversions

/**
 * Represents the MOP Agent's resource
 *
 * @version $Revision : 1.1 $
 */
class ProcessResource(val agent: MopAgent, val id: String, val process: MopProcess) extends ResourceSupport {

  def uri = UriBuilder.fromResource(classOf[ProcessResource]).build(id)

  @DELETE
  def delete(): Unit = {
    log.info("Killing process " + process)
    process.stop()
  }

  @POST
  @Consumes(Array(TEXT_PLAIN, TEXT_HTML, TEXT_XML, APPLICATION_XML))
  def post(@Context uriInfo: UriInfo, @Context headers: HttpHeaders, body: String): Unit = {
    if (body != null && body.toLowerCase == "kill") {
      delete()
    }
    else {
      log.warn("Unknown status '" + body + "' sent to process " + process)
    }
  }

  @POST
  @Consumes(Array("application/x-www-form-urlencoded"))
  def postMessageForm(@Context uriInfo: UriInfo, @Context headers: HttpHeaders, formData: Form) : Response = {
    log.info("<<<<<< received form: " + formData)
    import JavaConversions._
    for (key <- formData.keySet) {
      post(uriInfo, headers, key)
    }
    Response.seeOther(new URI("/processes")).build
  }

  @Path("directory")
  def directory() = {
    new RootDirectoryResource(process.getWorkDirectory, Agents.directoryLink(process))
  }
}