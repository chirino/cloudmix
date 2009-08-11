package org.fusesource.cloudmix.agent.resources

import javax.annotation.PostConstruct
import javax.ws.rs.core.{UriInfo, Context}
import mop.MopAgent

import _root_.com.sun.jersey.spi.inject.Inject
import _root_.javax.ws.rs.{Path}
/**
 * Represents the MOP Agent's resource
 *
 * @version $Revision : 1.1 $
 */
@Path("/")
//@Singleton
class AgentResource extends ResourceSupport {
  @Inject
  var agent: MopAgent = null
  @Context
  var uriInfo: UriInfo = null;

  @Path("processes")
  def processResources = new ProcessesResource(agent)

  @PostConstruct
  def start: Unit = {
    if (uriInfo == null) {
      log.error("no uriInfo injected!");
    }
    else if (agent == null) {
      log.error("no agent injected!")
    }
    else {
      // this only needs to be done once on startup
      // but we can't use @Singleton as
      // then we cannot get injected the uriInfo
      agent.setBaseHref(uriInfo.getBaseUri().toString);
    }
  }

}