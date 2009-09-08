package org.fusesource.cloudmix.agent.resources

import _root_.org.fusesource.cloudmix.agent.mop.{MopProcess, MopAgent}

import _root_.com.sun.jersey.api.representation.Form
import _root_.java.net.URI
import _root_.javax.ws.rs.core.MediaType._
import _root_.javax.ws.rs.core.{Response, HttpHeaders, UriInfo, Context}
import _root_.javax.ws.rs.{Consumes, POST, DELETE}
import _root_.scala.collection.jcl.Conversions._
             
/**
 * Represents the MOP Agent's resource
 *
 * @version $Revision : 1.1 $
 */
class ProcessResource(val agent: MopAgent, val id: String, val process: MopProcess) extends ResourceSupport {

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
    for (key <- formData.keySet) {
      post(uriInfo, headers, key)
    }
    Response.seeOther(new URI("/processes")).build
  }

}