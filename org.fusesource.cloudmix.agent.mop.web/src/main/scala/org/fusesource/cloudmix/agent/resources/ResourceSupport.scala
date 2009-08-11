package org.fusesource.cloudmix.agent.resources


import cloudmix.common.dto.AgentDetails
import mop.MopAgent
import scalautil.Logging

import _root_.com.sun.jersey.api.view.ImplicitProduces
import _root_.javax.ws.rs.Produces

/**
 * Base class for resources
 *
 * @version $Revision : 1.1 $
 */
@ImplicitProduces(Array("text/html;qs=5"))
@Produces(Array("application/xml", "application/json", "text/xml", "text/json"))
abstract class ResourceSupport extends Logging {
  def agent: MopAgent

  def details: AgentDetails = {
    agent.getAgentDetails
  }

  def processes = agent.getProcesses()

}