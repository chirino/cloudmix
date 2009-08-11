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
class ProcessResource extends ResourceSupport {
  @Inject
  var agent : MopAgent = null

  def details : AgentDetails = {
    agent.getAgentDetails
  }


}