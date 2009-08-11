package org.fusesource.cloudmix.agent.resources


import cloudmix.common.dto.AgentDetails
import javax.annotation.PostConstruct
import javax.ws.rs.core.{UriInfo, Context}
import mop.MopAgent

import _root_.com.sun.jersey.spi.inject.Inject
import _root_.com.sun.jersey.spi.resource.Singleton
import _root_.com.sun.jersey.api.view.ImplicitProduces
import _root_.javax.ws.rs.{Path, Produces}
import scalautil.Logging
/**
 * Represents the MOP Agent's resource
 *
 * @version $Revision: 1.1 $
 */
@Path("/")
//@Singleton
//@ImplicitProduces(Array("text/html;qs=5"))
//@Produces(Array("application/xml", "application/json", "text/xml", "text/json"))
class AgentResource extends ResourceSupport {
  @Inject
  var agent : MopAgent = null
  @Context
  var uriInfo : UriInfo = null;

  def details : AgentDetails = {
    agent.getAgentDetails
  }

  @PostConstruct
  def start : Unit = {
    if (uriInfo == null) {
      log.error("no uriInfo injected!");
    }
    else if (agent == null) {
      log.error("no agent injected!")
    }
    else {
      // this only needs to be done once on startup
      // but we can't use a singleton as
      // then we cannot get injected the uriInfo
      agent.setBaseHref(uriInfo.getBaseUri().toString);
    }
  }

}