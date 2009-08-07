package org.fusesource.cloudmix.agent.resources


import cloudmix.common.dto.AgentDetails
import mop.MopAgent

import com.sun.jersey.api.view.ImplicitProduces
import _root_.javax.ws.rs.{Path, Produces}
/**
 * Represents the MOP Agent's resource
 *
 * @version $Revision: 1.1 $
 */
@Path("/")
@ImplicitProduces(Array("text/html;qs=5"))
@Produces(Array("application/xml", "application/json", "text/xml", "text/json"))
class AgentResource {
  // TODO inject
  val agent : MopAgent = null
 
  def details : AgentDetails = {
    agent.getAgentDetails
  }
}